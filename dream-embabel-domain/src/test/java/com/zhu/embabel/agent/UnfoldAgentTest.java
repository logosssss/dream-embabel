package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.tool.progressive.UnfoldingTool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.UnfoldNote;
import com.zhu.embabel.tool.DeskTools;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class UnfoldAgentTest {

    private final UnfoldAgent agent = new UnfoldAgent();

    @Test
    void catalogHidesTheInnerToolsUntilItIsOpened() {
        UnfoldingTool desk = UnfoldAgent.desk();
        Set<String> inner = desk.getInnerTools().stream()
                .map(tool -> tool.getDefinition().getName())
                .collect(Collectors.toSet());

        assertEquals(UnfoldAgent.ENTRY, desk.getDefinition().getName());
        assertEquals(Set.of(DeskTools.COUNT, DeskTools.MARK), inner);
        assertEquals("字数：2", new DeskTools().countChars("主题"));
        assertEquals("首字：主", new DeskTools().firstMark("主题"));
    }

    @Test
    void writeAttachesOnlyTheCatalogAndUsesTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("字数：11\nJava 是一门跨平台的编程语言。");

        UnfoldNote note = agent.write(new UserInput("用一句话介绍 Java"), context.ai());
        var tools = context.getLlmInvocations().get(0).getInteraction().getTools();

        assertEquals(UnfoldAgent.ENTRY, note.entry());
        assertEquals("字数：11\nJava 是一门跨平台的编程语言。", note.text());
        assertEquals(1, tools.size());
        assertEquals(UnfoldAgent.ENTRY, tools.getFirst().getDefinition().getName());
        assertTrue(tools.getFirst() instanceof UnfoldingTool);
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("desk"));
    }
}
