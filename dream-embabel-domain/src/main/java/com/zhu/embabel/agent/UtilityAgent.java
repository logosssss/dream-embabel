package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Cost;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.tool.TerminateAgentException;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.UtilityChoice;

/**
 * 两个动作都能直接达成目标。{@code PlannerType.UTILITY} 用价值减成本，选净效用更高的那一支。
 * 主题里有「急」时追问是 80，否则直接回答是 80。选中的动作调用默认模型写正文，然后结束进程。
 * 不能 {@code canRerun}：Utility 只要还有能动的动作就会再选效用最高的那支，同一支会一直重跑。
 */
@Agent(description = "按效用在追问和直接回答之间选择")
public class UtilityAgent {

    public static final String ASK = "askValue";

    public static final String ANSWER = "answerValue";

    public static final int HIGH = 80;

    public static final int LOW = 1;

    @Cost(name = ASK)
    public double askValue(UserInput input) {
        return urgent(input.getContent()) ? HIGH : LOW;
    }

    @Cost(name = ANSWER)
    public double answerValue(UserInput input) {
        return urgent(input.getContent()) ? LOW : HIGH;
    }

    @AchievesGoal(description = "先追问")
    @Action(valueMethod = ASK, cost = 0, description = "追问")
    public UtilityChoice ask(UserInput input, Ai ai, OperationContext context) {
        String text = ai.withDefaultLlm().generateText(Prompts.quoted("""
                用户主题如下。不要直接回答，只追问一个最关键的问题。
                """, input.getContent()));
        return finish(new UtilityChoice("ask", HIGH, LOW, Texts.required(text)), context);
    }

    @AchievesGoal(description = "直接回答")
    @Action(valueMethod = ANSWER, cost = 0, description = "直接回答")
    public UtilityChoice answer(UserInput input, Ai ai, OperationContext context) {
        String text = ai.withDefaultLlm().generateText(Prompts.quoted("""
                用户主题如下。直接用一句中文回答，不要追问。
                """, input.getContent()));
        return finish(new UtilityChoice("answer", LOW, HIGH, Texts.required(text)), context);
    }

    private static UtilityChoice finish(UtilityChoice choice, OperationContext context) {
        context.addObject(choice);
        throw new TerminateAgentException("效用已选定");
    }


    static boolean urgent(String text) {
        return text != null && text.contains("急");
    }
}
