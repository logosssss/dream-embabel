package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.LlmTool;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.EchoLine;
import org.junit.jupiter.api.Test;

class EchoCallAgentTest {

    @Test
    void writeAttachesEchoAndUsesTheDefaultModel() {
        Tool echo = Tool.fromInstance(new StandIn()).getFirst();
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("跨平台，是这门语言最常被提起的一点。");

        EchoLine line = new EchoCallAgent(echo).write(new UserInput("用一句话介绍 Java"), context.ai());

        assertEquals("echo", line.tool());
        assertEquals("跨平台，是这门语言最常被提起的一点。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals("echo", call.getInteraction().getTools().getFirst().getDefinition().getName());
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains("四个字以内"));
    }

    @Test
    void closedEchoDoesNotCallTheModel() {
        FakeOperationContext context = FakeOperationContext.create();

        EchoUnavailableException exception = assertThrows(
                EchoUnavailableException.class,
                () -> new EchoCallAgent((Tool) null).write(new UserInput("用一句话介绍 Java"), context.ai()));

        assertEquals(EchoUnavailableException.MESSAGE, exception.getMessage());
        assertTrue(context.getLlmInvocations().isEmpty());
    }

    static class StandIn {

        @LlmTool(name = "echo", description = "把 text 原样返回")
        public String echo(String text) {
            return text;
        }
    }
}
