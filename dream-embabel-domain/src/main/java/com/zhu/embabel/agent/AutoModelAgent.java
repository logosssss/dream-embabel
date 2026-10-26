package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.AutoNote;

/**
 * 模型选择四档里的自动档。{@code withAutoLlm} 让框架按这次任务挑模型，
 * 不走默认模型，也不走 writer / reviewer。
 */
@Agent(description = "用自动选中的模型写一句说明")
public class AutoModelAgent {

    @AchievesGoal(description = "用自动模型写出一句说明")
    @Action(description = "withAutoLlm 一次调用")
    public AutoNote note(UserInput input, Ai ai) {
        return ai.withAutoLlm()
                .creating(AutoNote.class)
                .fromPrompt("""
                        用一句中文说明你准备怎么处理下面这句话。
                        不要复述用户原话，不要分步骤。

                        # 用户输入
                        %s
                        """.formatted(input.getContent()));
    }
}
