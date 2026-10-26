package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.SubagentLine;
import org.junit.jupiter.api.Test;

class SubagentHostAgentTest {

    @Test
    void hostAttachesTheSimpleLineAgentAsATool() {
        String name = SubagentHostAgent.CHILD.getDefinition().getName();
        String description = SubagentHostAgent.CHILD.getDefinition().getDescription();
        assertFalse(name.isBlank(), name);
        assertTrue(description.contains("简式"), description);

        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("简式写的是：Java 可以在不同系统上跑。");
        UserInput input = new UserInput("用一句话介绍 Java");

        SubagentLine line = new SubagentHostAgent().write(input, context.ai());

        assertEquals(name, line.tool());
        assertEquals("简式写的是：Java 可以在不同系统上跑。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(name, call.getInteraction().getTools().getFirst().getDefinition().getName());
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains(name));
    }
}
