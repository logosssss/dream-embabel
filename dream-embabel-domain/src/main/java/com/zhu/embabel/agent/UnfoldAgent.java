package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.progressive.UnfoldingTool;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.UnfoldNote;
import com.zhu.embabel.tool.DeskTools;

/**
 * 先只给模型一个目录工具。它调用目录之后，框架才把里面的方法放出来。
 */
@Agent(description = "先展开目录再调用里面的工具")
public class UnfoldAgent {

    public static final String ENTRY = "desk";

    @AchievesGoal(description = "展开目录后数字数并写一句")
    @Action(description = "先调用 desk，再用 countChars")
    public UnfoldNote write(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().withTool(desk()).generateText(Prompts.topic("""
                用户主题如下。必须先调用 desk 展开工具，再用 countChars 数字数。
                第一行原样写 countChars 的返回，第二行用一句中文回答主题。
                """, input.getContent()));
        return new UnfoldNote(ENTRY, Texts.required(text));
    }

    static UnfoldingTool desk() {
        return UnfoldingTool.of(
                ENTRY,
                "文具目录。调用后才会出现数字数和取首字。做题前必须先调用本工具。",
                Tool.fromInstance(new DeskTools()),
                true,
                "数字数用 countChars，取首字用 firstMark。");
    }

}
