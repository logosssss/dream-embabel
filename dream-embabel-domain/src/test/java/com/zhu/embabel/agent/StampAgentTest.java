package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.StampNote;
import com.zhu.embabel.tool.StampTool;
import org.junit.jupiter.api.Test;

class StampAgentTest {

    private final StampAgent agent = new StampAgent();

    @Test
    void fromInstanceExposesTheStampMethod() {
        String topic = "用一句话介绍 Java";
        int count = topic.codePointCount(0, topic.length());
        Tool tool = Tool.fromInstance(new StampTool()).getFirst();

        assertEquals(StampTool.NAME, tool.getDefinition().getName());
        assertEquals("章：" + count + "字", new StampTool().stamp(topic));
        assertTrue(tool.getDefinition().getDescription().contains("章：N字"));
    }

    @Test
    void writeAttachesThatToolAndUsesTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("章：11字\nJava 是一门跨平台的编程语言。");

        StampNote note = agent.write(new UserInput("用一句话介绍 Java"), context.ai());

        assertEquals(StampTool.NAME, note.tool());
        assertEquals("章：11字\nJava 是一门跨平台的编程语言。", note.text());
        assertEquals(
                StampTool.NAME,
                context.getLlmInvocations().get(0).getInteraction().getTools().getFirst().getDefinition().getName());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("stamp"));
    }
}
