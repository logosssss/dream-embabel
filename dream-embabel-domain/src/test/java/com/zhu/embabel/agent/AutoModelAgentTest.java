package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.AutoModelSelectionCriteria;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.AutoNote;
import org.junit.jupiter.api.Test;

class AutoModelAgentTest {

    private final AutoModelAgent agent = new AutoModelAgent();

    @Test
    void noteAsksForAnAutomaticModel() {
        FakeOperationContext context = FakeOperationContext.create();
        AutoNote expected = new AutoNote("先看这句话在问什么。");
        context.expectResponse(expected);

        AutoNote actual = agent.note(new UserInput("帮我看一下"), context.ai());

        assertEquals(expected, actual);
        assertEquals(1, context.getLlmInvocations().size());
        LlmInvocation call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(AutoModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(!(call.getInteraction().getLlm().getCriteria() instanceof DefaultModelSelectionCriteria));
        assertTrue(call.getPrompt().contains("不要复述用户原话"));
        assertTrue(call.getPrompt().contains("帮我看一下"));
    }
}
