package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.workflow.loop.Attempt;
import com.embabel.agent.api.common.workflow.loop.EvaluationActionContext;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableActionContext;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableBuilder;
import com.embabel.agent.api.common.workflow.loop.RepeatUntilAcceptableContext;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.PolishedLine;
import com.zhu.embabel.domain.ReflectiveNote;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.functions.Function1;

/**
 * 达成目标的动作内部用 {@link RepeatUntilAcceptableBuilder} 改写。
 * 第一轮固定 0.40，低于 0.80 就带着意见再写；第二轮固定 0.90。最多 2 轮。
 * 分数不交给模型，两句正文都用默认模型按主题写。
 */
@Agent(description = "反思循环直到分数够")
public class ReflectiveAgent {

    public static final int MAX = 2;

    public static final double BAR = 0.8;

    public static final double LOW = 0.4;

    public static final double HIGH = 0.9;

    static final String NO_EARLIER = "还没有上一轮句子。";

    static final String NO_FEEDBACK = "还没有上一轮意见。";

    static final String REVISE = "太短，补成完整的一句。";

    static final String DONE = "可以定稿。";

    @AchievesGoal(description = "把主题写成一句并通过反思")
    @Action(description = "按主题写一句，分数不够就再写一轮")
    public ReflectiveNote reflect(UserInput input, ActionContext context) {
        List<String> drafts = new ArrayList<>();
        PolishedLine line = RepeatUntilAcceptableBuilder.returning(PolishedLine.class)
                .withMaxIterations(MAX)
                .withScoreThreshold(BAR)
                .repeating(rewrite(input, drafts))
                .withEvaluator(judge(drafts))
                .build()
                .asSubProcess(context, PolishedLine.class);
        if (line == null || drafts.isEmpty()) {
            throw new IllegalStateException("反思循环没有写出句子。");
        }
        String discarded = drafts.size() > 1 ? drafts.get(0) : null;
        double discardedScore = discarded == null ? 0 : LOW;
        return new ReflectiveNote(drafts.size(), scoreFor(drafts.size()), discardedScore, discarded, line.text());
    }

    /** 按当前是第几轮给固定分。第一轮低于通过线，第二轮通过。 */
    static TextFeedback feedback(int round) {
        if (round <= 1) {
            return new TextFeedback(LOW, REVISE);
        }
        return new TextFeedback(HIGH, DONE);
    }

    static double scoreFor(int round) {
        return feedback(round).getScore();
    }

    /**
     * 写一轮句子。上一轮句子和意见都写进提示词，让第二轮按主题改，而不是另起一句无关的话。
     */
    String write(UserInput input, Ai ai, String earlier, String feedback) {
        String text = ai.withDefaultLlm().generateText(Prompts.quotedThen("""
                用户主题如下。只写一句中文。
                如果已经有上一轮句子和意见，按意见改写那一句。
                """, input.getContent(), """
                # 上一轮句子
                %s

                # 上一轮意见
                %s
                """.formatted(earlier, feedback)));
        return Texts.required(text);
    }

    private Function1<RepeatUntilAcceptableActionContext<Object, PolishedLine, TextFeedback>, PolishedLine> rewrite(
            UserInput input, List<String> drafts) {
        return ctx -> {
            String text = write(input, ctx.ai(), earlier(ctx), ctx.lastFeedbackOr(NO_FEEDBACK));
            drafts.add(text);
            return new PolishedLine(text);
        };
    }

    private Function1<EvaluationActionContext<Object, PolishedLine, TextFeedback>, TextFeedback> judge(
            List<String> drafts) {
        return ctx -> feedback(drafts.size());
    }

    private static String earlier(RepeatUntilAcceptableContext<?, PolishedLine, TextFeedback> ctx) {
        Attempt<PolishedLine, TextFeedback> attempt = ctx.lastAttempt();
        if (attempt == null || attempt.getResult() == null || attempt.getResult().text() == null
                || attempt.getResult().text().isBlank()) {
            return NO_EARLIER;
        }
        return attempt.getResult().text();
    }

}
