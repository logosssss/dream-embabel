package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.zhu.embabel.domain.QuickReply;

/**
 * 一次补全就返回。不挂工具，不审校，不改写。
 */
@Agent(description = "用一两句话直接回答用户")
public class QuickReplyAgent {

    /** 短回答的输出上限，避免模型把一次对话写成一篇稿。 */
    public static final int MAX_TOKENS = 256;

    @AchievesGoal(description = "直接回答用户的一句话")
    @Action(description = "一次模型调用，直接回答")
    public QuickReply reply(UserInput input, Ai ai) {
        return ai.withLlm(LlmOptions.withDefaultLlm().withTemperature(0.2).withMaxTokens(MAX_TOKENS))
                .creating(QuickReply.class)
                .fromPrompt("""
                        用一两句中文直接回答，不超过 80 字。
                        不要分步骤，不要调用工具，不要写成文章。

                        # 用户输入
                        %s
                        """.formatted(input.getContent()));
    }
}
