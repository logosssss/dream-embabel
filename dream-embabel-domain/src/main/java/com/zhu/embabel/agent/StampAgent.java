package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.StampNote;
import com.zhu.embabel.tool.StampTool;

/**
 * 用 {@link Tool#fromInstance} 把 {@link StampTool} 收成一个工具，挂到默认模型上。
 */
@Agent(description = "用 fromInstance 挂上盖章工具")
public class StampAgent {

    @AchievesGoal(description = "先盖章再写一句")
    @Action(description = "调用 stamp 后写一句")
    public StampNote write(UserInput input, Ai ai) {
        Tool tool = Tool.fromInstance(new StampTool()).getFirst();
        String text = ai.withDefaultLlm().withTool(tool).generateText("""
                用户主题如下。必须先调用 stamp 工具，把返回的章原样放在第一行。
                然后另起一行，用一句中文回答主题。不要解释，不要照抄主题原句。

                # 用户主题
                %s
                """.formatted(input.getContent()));
        return new StampNote(StampTool.NAME, Texts.required(text));
    }

}
