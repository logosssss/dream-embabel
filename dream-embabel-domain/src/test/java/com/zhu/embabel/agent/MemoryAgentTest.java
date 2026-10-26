package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.MemoryLine;
import org.junit.jupiter.api.Test;

class MemoryAgentTest {

    @Test
    void plainTopicLoadsTheTermAndNotTheAvoidance() {
        MemoryAgent agent = new MemoryAgent();
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("春草指刚发芽的草，专栏标题要点题。");

        MemoryLine line = agent.write(new UserInput("用记忆写一句关于春草"), context.ai());

        assertEquals(MemoryAgent.TERM, line.label());
        assertEquals("春草", line.name());
        assertTrue(line.fact().contains("标题要点题"), line.fact());
        assertEquals("春草指刚发芽的草，专栏标题要点题。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用记忆写一句关于春草"));
        assertTrue(call.getPrompt().contains("标题要点题"));
        assertFalse(call.getPrompt().contains("景色抒情"));
    }

    @Test
    void reviewTopicLoadsTheAvoidance() {
        MemoryAgent agent = new MemoryAgent();
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("评论里不要把春草写成景色。");

        MemoryLine line = agent.write(new UserInput("按评论的记忆写一句关于春草"), context.ai());

        assertEquals(MemoryAgent.AVOID, line.label());
        assertTrue(line.fact().contains("景色抒情"), line.fact());
        assertFalse(context.getLlmInvocations().get(0).getPrompt().contains("标题要点题"));
    }
}
