package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.agentic.playbook.PlaybookTool;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.GateNote;
import com.zhu.embabel.tool.DeskTools;
import java.util.List;

/**
 * 剧本把门禁做在工具上。countChars 一开始就能用，firstMark 要等它调用过才解锁。
 * 模型如果先取首字，框架会拒绝并要求先数字数。最多 4 轮。
 */
@Agent(description = "按剧本先数字数再取首字")
public class GateAgent {

    public static final String ENTRY = "gate";

    public static final int MAX_ITERATIONS = 4;

    @AchievesGoal(description = "按剧本写一句")
    @Action(description = "先 countChars，再 firstMark")
    public GateNote write(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().withTool(playbook()).generateText(Prompts.topic("""
                用户主题如下。必须按剧本：先调用 countChars，再调用 firstMark。
                第一行原样写字数，第二行原样写首字，第三行用一句中文回答主题。
                """, input.getContent()));
        return new GateNote(ENTRY, Texts.required(text));
    }

    static PlaybookTool playbook() {
        List<Tool> tools = Tool.fromInstance(new DeskTools());
        Tool count = named(tools, DeskTools.COUNT);
        Tool mark = named(tools, DeskTools.MARK);
        return new PlaybookTool(ENTRY, "必须先数字数，才能取首字。")
                .withMaxIterations(MAX_ITERATIONS)
                .withTools(count)
                .withTool(mark)
                .unlockedBy(count);
    }

    private static Tool named(List<Tool> tools, String name) {
        return tools.stream()
                .filter(tool -> name.equals(tool.getDefinition().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("缺少工具 " + name));
    }

}
