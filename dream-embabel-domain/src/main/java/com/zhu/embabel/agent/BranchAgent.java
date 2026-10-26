package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.BranchNote;
import java.util.Locale;

/**
 * 用 {@link Condition} 把同一个目标拆成两支。主题里出现「代码」或 code 才走代码支，
 * 否则走普通支。被放行的那一支调用默认模型，按主题写一句。
 */
@Agent(description = "按主题是否提到代码选择说明")
public class BranchAgent {

    /** 主题提到代码。{@link Action#pre} 用这个名字。 */
    public static final String CODE = "mentionsCode";

    /** 主题没有提到代码。和 {@link #CODE} 互斥。 */
    public static final String PLAIN = "plainTopic";

    @Condition(name = CODE)
    public boolean mentionsCode(UserInput input) {
        return mentions(input.getContent());
    }

    @Condition(name = PLAIN)
    public boolean plainTopic(UserInput input) {
        return !mentions(input.getContent());
    }

    @AchievesGoal(description = "代码主题的说明")
    @Action(pre = {CODE}, description = "代码分支")
    public BranchNote codeNote(UserInput input, Ai ai) {
        return new BranchNote("code", line(ai, input.getContent(), "只指出这段主题里一个最该看的代码问题。"));
    }

    @AchievesGoal(description = "普通主题的说明")
    @Action(pre = {PLAIN}, description = "普通分支")
    public BranchNote plainNote(UserInput input, Ai ai) {
        return new BranchNote("plain", line(ai, input.getContent(), "只用一句中文回应这个主题。"));
    }

    private static String line(Ai ai, String topic, String job) {
        String text = ai.withDefaultLlm().generateText(Prompts.quoted("用户主题如下。" + job, topic));
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("模型没有返回正文。");
        }
        return text.trim();
    }

    static boolean mentions(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return text.contains("代码") || text.toLowerCase(Locale.ROOT).contains("code");
    }
}
