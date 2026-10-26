package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.RoleLine;

/**
 * 代码只点角色名。writer 和 reviewer 分别对应配置里的模型，换模型不用改这里。
 * 主题里有「审」用 reviewer，否则用 writer。
 */
@Agent(description = "按角色选模型写一句")
public class RoleAgent {

    public static final String WRITER = "writer";

    public static final String REVIEWER = "reviewer";

    @AchievesGoal(description = "按角色写一句")
    @Action(description = "用 writer 或 reviewer 写一句")
    public RoleLine write(UserInput input, Ai ai) {
        String role = roleFor(input.getContent());
        String text = ai.withLlmByRole(role).generateText(prompt(role, input.getContent()));
        return new RoleLine(role, Texts.required(text));
    }

    static String roleFor(String text) {
        return text != null && text.contains("审") ? REVIEWER : WRITER;
    }

    private static String prompt(String role, String topic) {
        String job = WRITER.equals(role)
                ? "直接用一句中文介绍这个主题，不要提修改意见。"
                : "不要重写，只指出这个主题里一个最该改的问题。";
        return Prompts.quoted("用户主题如下。" + job, topic);
    }

}
