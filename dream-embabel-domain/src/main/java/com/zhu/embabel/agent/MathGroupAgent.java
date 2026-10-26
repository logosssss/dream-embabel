package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.MathAnswer;

/**
 * 把预置的 math 工具组挂到默认模型上。
 * 这个组对外先是一个名为 math 的展开工具，模型展开后才能做加法。
 */
@Agent(description = "用 math 工具组做加法")
public class MathGroupAgent {

    public static final String GROUP = CoreToolGroups.MATH;

    @AchievesGoal(description = "用数学工具组算出结果")
    @Action(description = "把题目交给 math 工具组")
    public MathAnswer sum(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().withToolGroup(GROUP).generateText("""
                题目如下。必须先调用名为 math 的工具，展开后再用加法计算，不要心算。
                只写出算式和结果，不要解释。

                # 题目
                %s
                """.formatted(input.getContent()));
        return new MathAnswer(GROUP, Texts.required(text));
    }

}
