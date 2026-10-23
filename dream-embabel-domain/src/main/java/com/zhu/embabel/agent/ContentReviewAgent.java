package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.annotation.Provided;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.api.common.workflow.loop.Attempt;
import com.embabel.agent.api.common.workflow.loop.EvaluationActionContext;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableActionContext;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableBuilder;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableContext;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.ReplanRequestedException;
import com.embabel.agent.tools.mcp.McpToolGroup;
import com.embabel.agent.core.hitl.WaitKt;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.zhu.embabel.domain.AcceptedDraft;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.Review;
import com.zhu.embabel.domain.ReviewNote;
import com.zhu.embabel.knowledge.HouseStyleTools;
import com.zhu.embabel.knowledge.StyleGuide;
import java.util.Locale;
import kotlin.jvm.functions.Function1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

/**
 * 内容审校 Agent。四个动作只声明自己读什么类型、产出什么类型，类里没有
 * 「先写再审再改」的调用代码。规划器看到目标是 {@link FinalArticle} 后，在本地搜出：
 * {@code draftContent → acceptDraft → reviewDraft → finalize}。
 *
 * <p>会打模型的是初稿、审校，以及定稿内部的改写和打分。确认在默认配置下不打模型。
 * 创作和改写挂了本地规范和检索，模型一旦调用工具就会再来一轮，所以一次成稿是多次串行补全。
 *
 * <p>{@link #EXPORTED_GOAL} 是对外目标名。本系统的 HTTP 和 shell 不靠这个名字，
 * 它们按 {@link FinalArticle} 做 {@code AgentInvocation}。
 */
@Agent(description = "按主题写中文初稿，审校后再定稿")
public class ContentReviewAgent {

    /** 写初稿和定稿改写。对应配置 {@code embabel.models.llms.writer}。 */
    public static final String WRITER_ROLE = "writer";
    /** 审校和定稿打分。对应配置 {@code embabel.models.llms.reviewer}。 */
    public static final String REVIEWER_ROLE = "reviewer";
    /**
     * 对外目标名。{@code remote = true} 只表示这个目标可以被远程客户端按名字找到，
     * 本进程不会因此变成 MCP server。起点类型是用户原文。
     */
    public static final String EXPORTED_GOAL = "reviewAndFinalize";
    /**
     * 定稿里改写的上限。框架默认是 5 轮、0.9 分；这里收成 3 轮，
     * 通过线用 {@code min-score / 100}，和审校的 0 到 100 对齐到框架的 0 到 1。
     */
    public static final int POLISH_ITERATIONS = 3;

    /** 初稿正文上限，写入提示词，不裁模型输出。 */
    private final int draftWords;
    /** 审校问题和建议的合计上限。 */
    private final int reviewWords;
    /** 定稿正文上限。 */
    private final int finalWords;
    /** 审校 0 到 100 的及格线，也是定稿 0 到 1 打分线的分子。 */
    private final int minScore;
    /** 为 true 时，确认初稿会把进程停在 WAITING，等 shell 的 x。 */
    private final boolean confirm;
    /** 本地栏目规范。挂上之后，模型调用它会多一轮。 */
    private final HouseStyleTools houseStyle;
    /** 进程内 Lucene 规范。{@code withReference} 会带上检索和章节展开工具。 */
    private final StyleGuide styleGuide;
    /** 用户原文为空白时拒绝这次模型调用，本身不打模型。 */
    private final TopicGuardRail topicGuardRail;
    /** 可选的本机 echo 工具。没有对应 bean 时为 null，提示词里也不提它。 */
    private final Tool echoTool;
    /** 可选的远程 MCP 工具组。没配地址时为 null。 */
    private final McpToolGroup remoteTools;

    /**
     * 字数、及格线和是否确认都来自配置，缺省值与 {@code application.properties} 一致。
     * echo 和远程工具没有 bean 时允许为空，避免没开 MCP 就启动失败。
     */
    public ContentReviewAgent(
            @Value("${dreamembabel.review.draft-words:400}") int draftWords,
            @Value("${dreamembabel.review.review-words:200}") int reviewWords,
            @Value("${dreamembabel.review.final-words:500}") int finalWords,
            @Value("${dreamembabel.review.min-score:60}") int minScore,
            @Value("${dreamembabel.review.confirm:false}") boolean confirm,
            HouseStyleTools houseStyle,
            StyleGuide styleGuide,
            TopicGuardRail topicGuardRail,
            @Nullable @EchoTool Tool echoTool,
            @Nullable McpToolGroup remoteTools) {
        this.draftWords = draftWords;
        this.reviewWords = reviewWords;
        this.finalWords = finalWords;
        this.minScore = minScore;
        this.confirm = confirm;
        this.houseStyle = houseStyle;
        this.styleGuide = styleGuide;
        this.topicGuardRail = topicGuardRail;
        this.echoTool = echoTool;
        this.remoteTools = remoteTools;
    }

    /**
     * 产出 {@link Draft}。规划器只要还没有初稿，就会选这个动作。
     *
     * <p>{@code canRerun = true} 是给低分重规划用的：初稿被藏起后，同一动作还要再跑。
     * {@link ReviewMemory} 标了 {@link Provided}，它是进程内的 Spring bean，不是前一个动作的产出，
     * 所以不会在计划里多出一步。第一次没有审校记录，提示词里就不附那一节。
     *
     * <p>温度 0.7，走 writer。规范和检索都挂在 {@link #writer} 上。
     */
    @Action(description = "根据用户主题生成中文初稿", canRerun = true)
    public Draft draftContent(UserInput input, @Provided ReviewMemory memory, Ai ai) {
        return writer(ai, LlmOptions.withLlmForRole(WRITER_ROLE).withTemperature(0.7))
                .creating(Draft.class)
                .fromPrompt("""
                        写一篇中文初稿。正文不超过 %d 字。
                        只产出标题和正文。用用户输入当主题，不要另起一个无关题目。
                        栏目规范调用 houseStyle 查询，不要凭空编造体例。
                        资料用检索工具查询，不要凭空编造事实。%s
                        如果下面附了审校记录，吸收其中的问题，不要再犯同样的错。

                        # 用户输入
                        %s
                        %s
                        """.formatted(draftWords, extraHints(), input.getContent(), priorReview(memory)));
    }

    /**
     * 把 {@link Draft} 收成 {@link AcceptedDraft}，好让审校动作有一个「已经过确认」的输入。
     * 默认不打模型，只做字段拷贝。
     *
     * <p>{@code dreamembabel.review.confirm=true} 时，{@link WaitKt#confirm} 抛出等待，
     * 进程停在 {@code WAITING}。人用 shell 的 {@code x} 确认后，这个动作才能真正返回。
     * {@code canRerun = true} 同样是为了低分重写之后还能再确认一次。
     */
    @Action(description = "确认初稿后才进入审校", canRerun = true)
    public AcceptedDraft acceptDraft(Draft draft) {
        AcceptedDraft accepted = new AcceptedDraft(draft.title(), draft.body());
        if (!confirm) {
            return accepted;
        }
        return WaitKt.confirm(accepted, "确认采用这篇初稿并进入审校？标题：" + draft.title());
    }

    /**
     * 产出 {@link Review}。要同时看见用户原文、初稿和已接受初稿，所以排在确认之后。
     *
     * <p>温度 0.2，走 reviewer。这里不挂 {@code houseStyle}，只挂规范检索和空白主题护栏。
     * 分数先记进 {@link ReviewMemory}，下一轮初稿才能读到。低于 {@link #minScore} 时不返回审校结果，
     * 而是要求重规划，并把初稿和已接受初稿从黑板上藏起来。藏起来之后，它们不再满足后续动作的前置条件，
     * 规划器就会重新选择 {@link #draftContent}。
     */
    @Action(description = "审校初稿并给出问题和修改建议", canRerun = true)
    public Review reviewDraft(
            UserInput input, Draft draft, AcceptedDraft accepted, @Provided ReviewMemory memory, Ai ai) {
        Review review = ai.withLlm(LlmOptions.withLlmForRole(REVIEWER_ROLE).withTemperature(0.2))
                .withPromptContributor(ContentReviewPersonas.REVIEWER)
                .withGuardRails(topicGuardRail)
                .withReference(styleGuide.reference())
                .creating(Review.class)
                .fromPrompt("""
                        审校下面的中文初稿。问题和建议合计不超过 %d 字。
                        score 取 0 到 100 的整数。对照用户原始要求，指出跑题、事实、逻辑和表达问题。
                        规范用检索工具查询，不要凭空编造体例。

                        # 用户输入
                        %s

                        # 标题
                        %s

                        # 正文
                        %s
                        """.formatted(reviewWords, input.getContent(), accepted.title(), accepted.body()));
        memory.remember(ReviewNote.from(review));
        if (review.score() < minScore) {
            throw new ReplanRequestedException(
                    "审校分数 " + review.score() + " 低于 " + minScore + "，丢掉初稿后重写",
                    blackboard -> {
                        blackboard.hide(draft);
                        blackboard.hide(accepted);
                    });
        }
        return review;
    }

    /**
     * 达成目标的动作。前置条件是用户原文、初稿和已经及格的 {@link Review}，
     * 所以低分重规划还没产出合格审校时，规划器不会选它。
     *
     * <p>方法体不直接打模型。它在当前进程里开一个子流程：改写一篇 {@link FinalArticle}，
     * 再打分；分数达到 {@link #polishThreshold()} 就停，否则把打分意见喂给下一轮改写。
     * 最多 {@link #POLISH_ITERATIONS} 轮。这个循环不代替 {@link #reviewDraft} 的重规划，
     * 审校不及格时根本走不到这里。
     *
     * <p>构建器的回调类型来自 Kotlin，所以用 {@link Function1} 适配，而不是 Java 的函数式接口。
     */
    @AchievesGoal(
            description = "结合审校意见完成中文定稿",
            export = @Export(
                    remote = true,
                    name = EXPORTED_GOAL,
                    startingInputTypes = UserInput.class))
    @Action(description = "根据初稿和审校意见写出定稿")
    public FinalArticle finalize(UserInput input, Draft draft, Review review, ActionContext context) {
        return RepeatUntilAcceptableBuilder.returning(FinalArticle.class)
                .withMaxIterations(POLISH_ITERATIONS)
                .withScoreThreshold(polishThreshold())
                .repeating(rewriteUntil(input, draft, review))
                .withEvaluator(judgeDraft(input, review))
                .build()
                .asSubProcess(context, FinalArticle.class);
    }

    /**
     * 定稿循环里的改写。审校分仍走 {@link #reviewDraft} 的重规划，这里只按已经及格的意见改文章。
     * 包内可见，供单测直接调用。走 {@link #writer}，因此同样可能因工具多一轮模型调用。
     * 温度不另设。
     *
     * @param previousFeedback 上一轮打分的中文意见；第一轮还没有意见时由调用方传入说明文字
     */
    FinalArticle rewrite(UserInput input, Draft draft, Review review, Ai ai, String previousFeedback) {
        return writer(ai, LlmOptions.withLlmForRole(WRITER_ROLE))
                .creating(FinalArticle.class)
                .fromPrompt("""
                        根据初稿和审校意见改写成中文定稿。正文不超过 %d 字。
                        吸收建议，不要把审校意见原样贴进正文。
                        栏目规范调用 houseStyle 查询，不要凭空编造体例。
                        资料用检索工具查询，不要凭空编造事实。%s
                        如果下面有上一轮定稿意见，按它改，不要重复同一处问题。

                        # 用户输入
                        %s

                        # 初稿标题
                        %s

                        # 初稿正文
                        %s

                        # 问题
                        %s

                        # 建议
                        %s

                        # 评分
                        %d

                        # 上一轮定稿意见
                        %s
                        """.formatted(
                        finalWords,
                        extraHints(),
                        input.getContent(),
                        draft.title(),
                        draft.body(),
                        review.issues(),
                        review.suggestions(),
                        review.score(),
                        previousFeedback));
    }

    /**
     * 定稿循环里的打分。产出框架的 {@link TextFeedback}，{@code score} 是 0 到 1，不是审校的 0 到 100。
     * 走 reviewer，温度 0.2。不挂栏目工具，只挂规范检索和空白主题护栏。包内可见，供单测直接调用。
     */
    TextFeedback judge(UserInput input, Review review, FinalArticle article, Ai ai) {
        return ai.withLlm(LlmOptions.withLlmForRole(REVIEWER_ROLE).withTemperature(0.2))
                .withPromptContributor(ContentReviewPersonas.REVIEWER)
                .withGuardRails(topicGuardRail)
                .withReference(styleGuide.reference())
                .creating(TextFeedback.class)
                .fromPrompt("""
                        给这篇定稿打分。score 取 0 到 1，达到 %s 才算通过。
                        feedback 用中文写还要改什么；通过时说明可以定稿。
                        规范用检索工具查询，不要凭空编造体例。

                        # 用户输入
                        %s

                        # 定稿
                        %s

                        # 原先的问题
                        %s

                        # 原先的建议
                        %s
                        """.formatted(
                        polishBar(),
                        input.getContent(),
                        article.getContent(),
                        review.issues(),
                        review.suggestions()));
    }

    /** 把子流程的改写回调接到 {@link #rewrite}。上一轮意见从循环上下文里取。 */
    private Function1<RepeatUntilAcceptableActionContext<Object, FinalArticle, TextFeedback>, FinalArticle>
            rewriteUntil(UserInput input, Draft draft, Review review) {
        return new Function1<RepeatUntilAcceptableActionContext<Object, FinalArticle, TextFeedback>, FinalArticle>() {
            @Override
            public FinalArticle invoke(
                    RepeatUntilAcceptableActionContext<Object, FinalArticle, TextFeedback> ctx) {
                return rewrite(input, draft, review, ctx.ai(), earlierFeedback(ctx));
            }
        };
    }

    /** 把子流程的打分回调接到 {@link #judge}。被打分的是本轮刚改写出来的定稿。 */
    private Function1<EvaluationActionContext<Object, FinalArticle, TextFeedback>, TextFeedback> judgeDraft(
            UserInput input, Review review) {
        return new Function1<EvaluationActionContext<Object, FinalArticle, TextFeedback>, TextFeedback>() {
            @Override
            public TextFeedback invoke(EvaluationActionContext<Object, FinalArticle, TextFeedback> ctx) {
                return judge(input, review, ctx.getResultToEvaluate(), ctx.ai());
            }
        };
    }

    /** 第一轮还没有打分，或意见是空白时，给改写提示词一句明确的占位，避免模型看到空段落。 */
    private static String earlierFeedback(RepeatUntilAcceptableContext<?, ?, TextFeedback> ctx) {
        Attempt<?, TextFeedback> attempt = ctx.lastAttempt();
        if (attempt == null || attempt.getFeedback() == null) {
            return "还没有上一轮意见。";
        }
        String feedback = attempt.getFeedback().getFeedback();
        if (feedback == null || feedback.isBlank()) {
            return "还没有上一轮意见。";
        }
        return feedback;
    }

    /** 审校及格线换成定稿循环使用的 0 到 1。60 分对应 0.60。 */
    private double polishThreshold() {
        return minScore / 100.0;
    }

    /** 写进打分提示词的通过线，固定两位小数，不随默认语言环境变成逗号。 */
    private String polishBar() {
        return String.format(Locale.ROOT, "%.2f", polishThreshold());
    }

    /**
     * 创作和定稿改写共用的模型入口。人格、空白主题护栏、栏目规范和规范检索总是挂上。
     * echo、远程工具只在对应 bean 存在时挂上，避免提示词提到一个并不存在的工具。
     *
     * <p>不在这里设温度。初稿传入 0.7，定稿改写沿用角色默认值。
     */
    private PromptRunner writer(Ai ai, LlmOptions options) {
        PromptRunner runner = ai.withLlm(options)
                .withPromptContributor(ContentReviewPersonas.WRITER)
                .withGuardRails(topicGuardRail)
                .withToolObject(houseStyle)
                .withReference(styleGuide.reference());
        if (remoteTools != null) {
            runner = runner.withToolGroup(remoteTools);
        }
        if (echoTool == null) {
            return runner;
        }
        return runner.withTool(echoTool);
    }

    /** 只有工具真的挂上了，才把对应句子拼进提示词。 */
    private String extraHints() {
        return echoHint() + remoteHint();
    }

    private String echoHint() {
        if (echoTool == null) {
            return "";
        }
        return "\n需要把一段文字原样带回时调用 echo。";
    }

    private String remoteHint() {
        if (remoteTools == null) {
            return "";
        }
        return "\n外部资料调用已挂上的远程工具，不要凭空编造。";
    }

    /**
     * 重规划后再写初稿时，把上一轮审校附进提示词。还没有记录时返回空串，
     * 第一次初稿的提示词里就不会出现「上一轮审校」。
     */
    private static String priorReview(ReviewMemory memory) {
        ReviewNote note = memory.latest();
        if (note == null) {
            return "";
        }
        return """

                # 上一轮审校
                问题：%s
                建议：%s
                评分：%d
                """.formatted(note.issues(), note.suggestions(), note.score());
    }
}
