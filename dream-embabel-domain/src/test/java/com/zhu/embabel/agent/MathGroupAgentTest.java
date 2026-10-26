package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.core.ToolGroupRequirement;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.MathAnswer;
import org.junit.jupiter.api.Test;

class MathGroupAgentTest {

    private final MathGroupAgent agent = new MathGroupAgent();

    @Test
    void sumAttachesTheMathToolGroupAndUsesTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("17 + 25 = 42");

        MathAnswer answer = agent.sum(new UserInput("把 17 和 25 相加"), context.ai());

        assertEquals(MathGroupAgent.GROUP, answer.group());
        assertEquals("17 + 25 = 42", answer.text());
        assertTrue(context.getLlmInvocations().get(0).getInteraction().getToolGroups().stream()
                .map(ToolGroupRequirement::getRole)
                .anyMatch(MathGroupAgent.GROUP::equals));
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("把 17 和 25 相加"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("名为 math 的工具"));
    }
}
