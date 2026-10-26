package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.hitl.WaitFor;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.ColumnBrief;
import com.zhu.embabel.domain.FormLine;

/**
 * 先停在表单。人提交之后，模型才根据填好的栏目要求写一句。
 */
@Agent(description = "等人填完表单再写一句")
public class FormAgent {

    public static final String TITLE = "补一条栏目要求";

    @Action(description = "停下来等人填写栏目要求")
    public ColumnBrief ask(UserInput input) {
        return WaitFor.formSubmission(TITLE, ColumnBrief.class);
    }

    @AchievesGoal(description = "按表单写一句")
    @Action(description = "按填好的栏目要求写一句")
    public FormLine write(ColumnBrief brief, Ai ai) {
        String text = ai.withDefaultLlm().generateText("""
                下面是人填的栏目要求。只用一句中文写成栏目说明。
                不要解释，不要照抄原句。

                # 栏目
                %s

                # 要点
                %s
                """.formatted(brief.column(), brief.point()));
        return new FormLine(Texts.required(text));
    }

}
