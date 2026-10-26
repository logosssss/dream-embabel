package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import org.junit.jupiter.api.Test;

class ReflectiveAgentTest {

    private final ReflectiveAgent agent = new ReflectiveAgent();

    @Test
    void firstRoundStaysUnderTheBarAndSecondRoundPasses() {
        assertTrue(agent.feedback(1).getScore() < ReflectiveAgent.BAR);
        assertEquals(ReflectiveAgent.LOW, agent.feedback(1).getScore());
        assertEquals(ReflectiveAgent.REVISE, agent.feedback(1).getFeedback());
        assertTrue(agent.feedback(2).getScore() >= ReflectiveAgent.BAR);
        assertEquals(ReflectiveAgent.HIGH, agent.feedback(2).getScore());
        assertEquals(ReflectiveAgent.DONE, agent.feedback(2).getFeedback());
    }

    @Test
    void bothRoundsCallTheDefaultModelWithTheTopic() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 是一门");
        context.expectResponse("Java 是一门跨平台的编程语言。");
        UserInput input = new UserInput("用一句话介绍 Java");

        String first = agent.write(input, context.ai(), ReflectiveAgent.NO_EARLIER, ReflectiveAgent.NO_FEEDBACK);
        String second = agent.write(input, context.ai(), first, ReflectiveAgent.REVISE);

        assertEquals("Java 是一门", first);
        assertEquals("Java 是一门跨平台的编程语言。", second);
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(1).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains(ReflectiveAgent.NO_EARLIER));
        assertTrue(context.getLlmInvocations().get(1).getPrompt().contains("Java 是一门"));
        assertTrue(context.getLlmInvocations().get(1).getPrompt().contains(ReflectiveAgent.REVISE));
    }
}
