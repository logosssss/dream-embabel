package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.ExportLine;

/**
 * 目标用 {@link Export} 标成可按名字远程找到。这里没有 MCP server 传输。
 */
@Agent(description = "按导出的目标名写一句")
public class ExportLineAgent {

    public static final String EXPORTED = "oneLine";

    @AchievesGoal(
            description = "写出一句",
            export = @Export(remote = true, name = EXPORTED, startingInputTypes = UserInput.class))
    @Action(description = "按主题写一句")
    public ExportLine write(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().generateText(Prompts.topic("""
                只用一句中文回应这个主题。
                """, input.getContent()));
        return new ExportLine(Texts.required(text));
    }

}
