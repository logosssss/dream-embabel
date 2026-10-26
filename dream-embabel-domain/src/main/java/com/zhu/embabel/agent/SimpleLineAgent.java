package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.SupplierActionContext;
import com.embabel.agent.api.common.workflow.control.SimpleAgentBuilder;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.SimpleLine;
import kotlin.jvm.functions.Function1;

/**
 * 生成这一步不写在 @Action 里，交给 SimpleAgentBuilder。
 */
@Agent(description = "用简式构建器写一句")
public class SimpleLineAgent {

    public static final String NAME = "simple-line";

    @AchievesGoal(description = "写出一句")
    @Action(description = "交给简式构建器写一句")
    public SimpleLine write(UserInput input, ActionContext context) {
        SimpleLine line = SimpleAgentBuilder.returning(SimpleLine.class)
                .running(runner(input))
                .asSubProcess(context);
        if (line == null || line.text() == null || line.text().isBlank()) {
            throw new IllegalStateException("简式构建器没有返回正文。");
        }
        return line;
    }

    static Function1<SupplierActionContext<SimpleLine>, SimpleLine> runner(UserInput input) {
        return ctx -> line(input, ctx.ai());
    }

    static SimpleLine line(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().generateText(Prompts.topic("""
                只用一句中文回应这个主题。
                """, input.getContent()));
        return new SimpleLine(Texts.required(text));
    }

}
