package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.NamedLine;
import org.junit.jupiter.api.Test;

class NamedLineAgentTest {

    private final NamedLineAgent agent = new NamedLineAgent();

    @Test
    void writeUsesTheDefaultModelAndKeepsTheCallId() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 是一门跨平台的编程语言。");

        NamedLine line = agent.write(new UserInput("用一句话介绍 Java"), context.ai());

        assertEquals(NamedLineAgent.CALL_ID, line.callId());
        assertEquals("Java 是一门跨平台的编程语言。", line.text());
        assertEquals(NamedLineAgent.CALL_ID, context.getLlmInvocations().get(0).getInteraction().getId());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("不要照抄主题原句"));
    }
}
