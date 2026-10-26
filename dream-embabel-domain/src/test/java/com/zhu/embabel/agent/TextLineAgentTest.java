package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.TextLine;
import org.junit.jupiter.api.Test;

class TextLineAgentTest {

    private final TextLineAgent agent = new TextLineAgent();

    @Test
    void lineUsesGenerateTextOnTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("春草发芽。");

        TextLine actual = agent.line(new UserInput("写春天的河边"), context.ai());

        assertEquals("春草发芽。", actual.text());
        assertEquals(1, context.getLlmInvocations().size());
        LlmInvocation call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("这是纯文本"));
        assertTrue(call.getPrompt().contains("不要照抄原句"));
        assertTrue(call.getPrompt().contains("写春天的河边"));
    }
}
