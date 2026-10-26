package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.common.workflow.control.SimpleAgentBuilder;
import com.embabel.agent.core.Agent;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.SimpleLine;
import org.junit.jupiter.api.Test;

class SimpleLineAgentTest {

    @Test
    void builderNamesTheAgentAndTheModelWritesOneSentence() {
        Agent agent = SimpleAgentBuilder.returning(SimpleLine.class)
                .running(ctx -> new SimpleLine("占位"))
                .buildAgent(SimpleLineAgent.NAME, "用简式构建器写一句");
        assertEquals(SimpleLineAgent.NAME, agent.getName());

        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 可以在不同系统上跑。");
        UserInput input = new UserInput("用一句话介绍 Java");

        SimpleLine line = SimpleLineAgent.line(input, context.ai());

        assertEquals("Java 可以在不同系统上跑。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains("只用一句中文回应"));
    }
}
