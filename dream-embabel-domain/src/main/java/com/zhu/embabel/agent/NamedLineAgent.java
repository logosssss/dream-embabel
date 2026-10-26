package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.NamedLine;

/**
 * 用默认模型写一句，并给这次调用标上固定 id。
 * 1.5.3 的 {@code withId} 在 {@code PromptRunner} 上，不在 {@code Ai} 上。
 */
@Agent(description = "给一次模型调用打上 id")
public class NamedLineAgent {

    public static final String CALL_ID = "named_line";

    @AchievesGoal(description = "写出带调用 id 的一句")
    @Action(description = "用默认模型写一句")
    public NamedLine write(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().withId(CALL_ID).generateText(Prompts.quoted("""
                用户主题如下。只写一句中文。
                """, input.getContent()));
        return new NamedLine(CALL_ID, Texts.required(text));
    }

}
