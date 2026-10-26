package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.common.workflow.control.ResultList;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.ScatterNote;
import com.zhu.embabel.domain.Take;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScatterAgentTest {

    private final ScatterAgent agent = new ScatterAgent();

    @Test
    void eachAngleCallsTheModelAndJoinKeepsBoth() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 可以在不同系统上跑。");
        context.expectResponse("Java 的启动和内存开销更大。");
        UserInput input = new UserInput("用一句话介绍 Java");

        Take benefit = agent.line(input, context.ai(), ScatterAgent.BENEFIT);
        Take cost = agent.line(input, context.ai(), ScatterAgent.COST);
        ScatterNote note = ScatterAgent.join(new ResultList<>(List.of(cost, benefit)));

        assertEquals(ScatterAgent.BENEFIT, benefit.angle());
        assertEquals("Java 可以在不同系统上跑。", benefit.text());
        assertEquals("Java 的启动和内存开销更大。", cost.text());
        assertEquals("Java 可以在不同系统上跑。", note.benefit());
        assertEquals("Java 的启动和内存开销更大。", note.cost());
        assertCall(context, 0, "好处");
        assertCall(context, 1, "代价");
    }

    private static void assertCall(FakeOperationContext context, int index, String angle) {
        var call = context.getLlmInvocations().get(index);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains(angle));
        assertTrue(call.getPrompt().contains("不要照抄主题原句"));
    }
}
