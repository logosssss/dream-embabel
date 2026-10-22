package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.Review;
import org.springframework.beans.factory.annotation.Value;

/**
 * 创作、审校、定稿三步。顺序由类型推导，这里不写调用顺序。
 */
@Agent(description = "按主题写中文初稿，审校后再定稿")
public class ContentReviewAgent {

    private final int draftWords;
    private final int reviewWords;
    private final int finalWords;

    public ContentReviewAgent(
            @Value("${dreamembabel.review.draft-words:400}") int draftWords,
            @Value("${dreamembabel.review.review-words:200}") int reviewWords,
            @Value("${dreamembabel.review.final-words:500}") int finalWords) {
        this.draftWords = draftWords;
        this.reviewWords = reviewWords;
        this.finalWords = finalWords;
    }

    @Action(description = "根据用户主题生成中文初稿")
    public Draft draftContent(UserInput input, Ai ai) {
        return ai.withLlm(LlmOptions.withAutoLlm().withTemperature(0.7))
                .withPromptContributor(ContentReviewPersonas.WRITER)
                .creating(Draft.class)
                .fromPrompt("""
                        写一篇中文初稿。正文不超过 %d 字。
                        只产出标题和正文。用用户输入当主题，不要另起一个无关题目。

                        # 用户输入
                        %s
                        """.formatted(draftWords, input.getContent()));
    }

    @Action(description = "审校初稿并给出问题和修改建议")
    public Review reviewDraft(UserInput input, Draft draft, Ai ai) {
        return ai.withLlm(LlmOptions.withAutoLlm().withTemperature(0.2))
                .withPromptContributor(ContentReviewPersonas.REVIEWER)
                .creating(Review.class)
                .fromPrompt("""
                        审校下面的中文初稿。问题和建议合计不超过 %d 字。
                        score 取 0 到 100 的整数。对照用户原始要求，指出跑题、事实、逻辑和表达问题。

                        # 用户输入
                        %s

                        # 标题
                        %s

                        # 正文
                        %s
                        """.formatted(reviewWords, input.getContent(), draft.title(), draft.body()));
    }

    @AchievesGoal(
            description = "结合审校意见完成中文定稿",
            export = @Export(remote = true, name = "reviewAndFinalize"))
    @Action(description = "根据初稿和审校意见写出定稿")
    public FinalArticle finalize(UserInput input, Draft draft, Review review, Ai ai) {
        return ai.withAutoLlm()
                .withPromptContributor(ContentReviewPersonas.WRITER)
                .creating(FinalArticle.class)
                .fromPrompt("""
                        根据初稿和审校意见改写成中文定稿。正文不超过 %d 字。
                        吸收建议，不要把审校意见原样贴进正文。

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
                        """.formatted(
                        finalWords,
                        input.getContent(),
                        draft.title(),
                        draft.body(),
                        review.issues(),
                        review.suggestions(),
                        review.score()));
    }
}
