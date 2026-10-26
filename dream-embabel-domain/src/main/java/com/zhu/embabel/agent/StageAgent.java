package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.Brief;
import com.zhu.embabel.domain.SupervisorNote;

/**
 * Supervisor 规划器把准备收成工具，只留一个达成目标的成稿动作。
 * 监督者自己调用模型决定先跑哪一步；准备稿也用默认模型写一句，成稿不再调用模型。
 */
@Agent(description = "监督者先准备再成稿", planner = PlannerType.SUPERVISOR)
public class StageAgent {

    public static final String STAGE = "prepare";

    @Action(description = "用户主题已经在输入里。写一句准备稿。这一步现在就能做，不要向用户追问。")
    public Brief prepare(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().generateText("""
                用户主题如下。只写一句准备稿，不要解释，不要加引号，不要照抄主题原句。

                # 用户主题
                %s
                """.formatted(input.getContent()));
        return new Brief(Texts.required(text));
    }

    @AchievesGoal(description = "准备稿已经写好后，把它收成最终结果")
    @Action(description = "只有黑板上已经有准备稿时才调用。把准备稿原样收成最终结果，不要再写新内容。")
    public SupervisorNote finish(Brief brief) {
        return new SupervisorNote(STAGE, brief.text());
    }

}
