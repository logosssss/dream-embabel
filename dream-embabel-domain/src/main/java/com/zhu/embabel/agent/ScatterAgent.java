package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.SupplierActionContext;
import com.embabel.agent.api.common.TransformationActionContext;
import com.embabel.agent.api.common.workflow.control.ResultList;
import com.embabel.agent.api.common.workflow.control.ScatterGatherBuilder;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.ScatterNote;
import com.zhu.embabel.domain.Take;
import java.util.List;
import java.util.function.Function;
import kotlin.jvm.functions.Function1;

/**
 * 同一主题分两路并行写。合成是普通 Java，不再调用模型。
 */
@Agent(description = "把主题分两路再合成")
public class ScatterAgent {

    public static final String BENEFIT = "好处";

    public static final String COST = "代价";

    @AchievesGoal(description = "写出好处和代价")
    @Action(description = "分两路写，再合成")
    public ScatterNote write(UserInput input, ActionContext context) {
        ScatterNote note = ScatterGatherBuilder.returning(ScatterNote.class)
                .fromElements(Take.class)
                .withGenerators(generators(input))
                .consolidatedBy(joinAll())
                .asSubProcess(context);
        if (note == null || note.benefit() == null || note.cost() == null) {
            throw new IllegalStateException("分发没有合成结果。");
        }
        return note;
    }

    /**
     * 写一路。提示词只要求这一个方面，正文必须和主题有关。
     */
    Take line(UserInput input, Ai ai, String angle) {
        String text = ai.withDefaultLlm().generateText(Prompts.quoted("""
                用户主题如下。只写一句中文，只谈「%s」。
                """.formatted(angle), input.getContent()));
        return new Take(angle, Texts.required(text));
    }

    /** 按方面把两路放回好处和代价。顺序不重要。 */
    static ScatterNote join(ResultList<Take> results) {
        return new ScatterNote(textOf(results, BENEFIT), textOf(results, COST));
    }

    private List<Function<SupplierActionContext<Take>, Take>> generators(UserInput input) {
        return List.of(
                ctx -> line(input, ctx.ai(), BENEFIT),
                ctx -> line(input, ctx.ai(), COST));
    }

    private Function1<TransformationActionContext<ResultList<Take>, ScatterNote>, ScatterNote> joinAll() {
        return ctx -> join(ctx.getInput());
    }

    private static String textOf(ResultList<Take> results, String angle) {
        if (results == null || results.getResults() == null) {
            throw new IllegalStateException("分发没有返回「" + angle + "」。");
        }
        for (Take take : results.getResults()) {
            if (take != null && angle.equals(take.angle()) && take.text() != null && !take.text().isBlank()) {
                return take.text().trim();
            }
        }
        throw new IllegalStateException("分发没有返回「" + angle + "」。");
    }

}
