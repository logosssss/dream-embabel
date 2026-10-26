package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Subagent;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.SubagentLine;

/**
 * 把简式代理收成一个工具。模型先调用它，再转述一句。
 */
@Agent(description = "把主题交给简式子代理再转述")
public class SubagentHostAgent {

    public static final Tool CHILD = Subagent.ofClass(SimpleLineAgent.class).consuming(UserInput.class);

    @AchievesGoal(description = "转述子代理写的一句")
    @Action(description = "先调用简式子代理，再转述")
    public SubagentLine write(UserInput input, Ai ai) {
        String name = CHILD.getDefinition().getName();
        String text = ai.withDefaultLlm().withTool(CHILD).generateText(Prompts.topic("""
                用户主题如下。必须先调用工具 %s，把主题交给它。
                然后只用一句中文转述它的结果。
                """.formatted(name), input.getContent()));
        return new SubagentLine(name, Texts.required(text));
    }

}
