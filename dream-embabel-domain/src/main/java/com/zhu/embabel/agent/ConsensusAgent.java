package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.SupplierActionContext;
import com.embabel.agent.api.common.TransformationActionContext;
import com.embabel.agent.api.common.workflow.control.ResultList;
import com.embabel.agent.api.common.workflow.multimodel.ConsensusBuilder;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.zhu.embabel.domain.ConsensusNote;
import java.util.List;
import java.util.function.Function;
import kotlin.jvm.functions.Function1;

/**
 * 同一问题分两路调用默认模型，温度不同。是否一致由代码比较第一行，不再问模型。
 */
@Agent(description = "两路判断是否一致")
public class ConsensusAgent {

    public static final String COOL = "低温";

    public static final String WARM = "高温";

    public static final double COOL_TEMPERATURE = 0.0;

    public static final double WARM_TEMPERATURE = 0.8;

    public static final String AGREED = "达成共识";

    public static final String SPLIT = "存疑";

    @AchievesGoal(description = "比较两路判断")
    @Action(description = "两路各判断一次，再比较")
    public ConsensusNote write(UserInput input, ActionContext context) {
        ConsensusNote note = ConsensusBuilder.returning(ConsensusNote.class)
                .withSources(sources(input))
                .withConsensusBy(compare())
                .asSubProcess(context);
        if (note == null || note.cool() == null || note.warm() == null) {
            throw new IllegalStateException("共识没有合成结果。");
        }
        return note;
    }

    /**
     * 问模型这个主题是不是在讲具体的事。第一行只能是「是」或「否」，第二行写一句理由。
     */
    ConsensusNote vote(UserInput input, Ai ai, String source, double temperature) {
        String text = ai.withLlm(LlmOptions.withDefaultLlm().withTemperature(temperature)).generateText("""
                用户主题如下。判断它是不是在讲一件具体的事。
                第一行只写「是」或「否」。第二行用一句中文说明理由。
                不要解释格式，不要照抄主题原句。

                # 用户主题
                %s
                """.formatted(input.getContent()));
        String body = Texts.required(text);
        if (COOL.equals(source)) {
            return new ConsensusNote(null, null, body, null);
        }
        return new ConsensusNote(null, null, null, body);
    }

    /** 两路第一行相同，并且是「是」或「否」，才算达成共识。 */
    static ConsensusNote join(ResultList<ConsensusNote> results) {
        String cool = side(results, true);
        String warm = side(results, false);
        String coolAnswer = answerOf(cool);
        String warmAnswer = answerOf(warm);
        boolean same = coolAnswer.equals(warmAnswer) && ("是".equals(coolAnswer) || "否".equals(coolAnswer));
        return new ConsensusNote(same ? AGREED : SPLIT, same ? coolAnswer : null, cool, warm);
    }

    private List<Function<SupplierActionContext<ConsensusNote>, ConsensusNote>> sources(UserInput input) {
        return List.of(
                ctx -> vote(input, ctx.ai(), COOL, COOL_TEMPERATURE),
                ctx -> vote(input, ctx.ai(), WARM, WARM_TEMPERATURE));
    }

    private Function1<TransformationActionContext<ResultList<ConsensusNote>, ConsensusNote>, ConsensusNote> compare() {
        return ctx -> join(ctx.getInput());
    }

    private static String side(ResultList<ConsensusNote> results, boolean cool) {
        if (results == null || results.getResults() == null) {
            throw new IllegalStateException("共识没有返回判断。");
        }
        for (ConsensusNote note : results.getResults()) {
            if (note == null) {
                continue;
            }
            String text = cool ? note.cool() : note.warm();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }
        throw new IllegalStateException(cool ? "共识没有返回低温。" : "共识没有返回高温。");
    }

    static String answerOf(String text) {
        String first = text.lines().findFirst().orElse("").trim();
        if (first.startsWith("是")) {
            return "是";
        }
        if (first.startsWith("否")) {
            return "否";
        }
        return "不清楚";
    }

}
