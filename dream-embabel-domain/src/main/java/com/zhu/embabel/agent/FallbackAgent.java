package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.zhu.embabel.domain.FallbackLine;

/**
 * 按名字顺序选第一个已登记的模型。missing-model 没有登记，所以会用 deepseek-flash。
 * 同一次调用只发给选中的那一个模型。
 */
@Agent(description = "按顺序选第一个可用模型写一句")
public class FallbackAgent {

    public static final String FIRST = "missing-model";

    public static final String SECOND = "deepseek-flash";

    @AchievesGoal(description = "按顺序写一句")
    @Action(description = "用第一个已登记的模型写一句")
    public FallbackLine write(UserInput input, Ai ai) {
        String text = ai.withLlm(LlmOptions.withFirstAvailableLlmOf(FIRST, SECOND)).generateText(Prompts.quoted("""
                用户主题如下。只用一句中文回应。
                """, input.getContent()));
        return new FallbackLine(FIRST, SECOND, Texts.required(text));
    }

}
