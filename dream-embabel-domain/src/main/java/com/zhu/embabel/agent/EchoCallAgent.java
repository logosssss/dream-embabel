package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.EchoLine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 把本机 echo MCP 工具交给模型。模型先调用它，再写一句。
 * 没有这个 Bean 时进程仍能启动，写的时候再拒绝。
 */
@Agent(description = "先调用 echo 再写一句")
public class EchoCallAgent {

    private final Tool echo;

    public EchoCallAgent(Tool echo) {
        this.echo = echo;
    }

    @Autowired
    public EchoCallAgent(@EchoTool ObjectProvider<Tool> echos) {
        this(echos.getIfAvailable());
    }

    @AchievesGoal(description = "用 echo 的返回写一句")
    @Action(description = "先调用 echo，再写一句")
    public EchoLine write(UserInput input, Ai ai) {
        if (echo == null) {
            throw new EchoUnavailableException();
        }
        String name = echo.getDefinition().getName();
        String text = ai.withDefaultLlm().withTool(echo).generateText(Prompts.topic("""
                用户主题如下。必须先调用工具 %s。
                text 参数只写一个四个字以内的中文短词，概括主题，不要把主题原句传进去。
                然后只用一句中文回应主题，用到工具返回的那个短词。
                """.formatted(name), input.getContent()));
        return new EchoLine(name, Texts.required(text));
    }

}
