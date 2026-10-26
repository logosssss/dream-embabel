package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.agentic.DomainToolTracker;
import com.embabel.agent.api.tool.agentic.playbook.ConditionalTool;
import com.embabel.agent.api.tool.agentic.playbook.PlaybookState;
import com.embabel.agent.api.tool.agentic.playbook.PlaybookTool;
import com.embabel.agent.api.tool.agentic.playbook.UnlockCondition;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.GateNote;
import com.zhu.embabel.tool.DeskTools;
import org.junit.jupiter.api.Test;

class GateAgentTest {

    private final GateAgent agent = new GateAgent();

    @Test
    void firstMarkStaysLockedUntilCountCharsHasRun() {
        PlaybookTool playbook = GateAgent.playbook();
        Tool mark = playbook.getLockedTools$embabel_agent_api().getFirst().getTool();
        PlaybookState state = new PlaybookState(FakeOperationContext.create(), new DomainToolTracker());
        ConditionalTool guarded = new ConditionalTool(mark, UnlockCondition.afterTools(DeskTools.COUNT), state);

        Tool.Result blocked = guarded.call("{\"topic\":\"主题\"}");
        state.recordToolCall(DeskTools.COUNT);
        Tool.Result opened = guarded.call("{\"topic\":\"主题\"}");

        assertEquals(1, playbook.getUnlockedToolCount());
        assertEquals(1, playbook.getLockedToolCount());
        assertEquals(GateAgent.ENTRY, playbook.getDefinition().getName());
        assertInstanceOf(Tool.Result.Text.class, blocked);
        assertTrue(((Tool.Result.Text) blocked).getContent().contains("You must first use: countChars"));
        assertInstanceOf(Tool.Result.Text.class, opened);
        assertEquals("首字：主", ((Tool.Result.Text) opened).getContent());
    }

    @Test
    void writeAttachesThePlaybookAndUsesTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("字数：2\n首字：主\n这是一句回答。");

        GateNote note = agent.write(new UserInput("主题"), context.ai());
        var tools = context.getLlmInvocations().get(0).getInteraction().getTools();

        assertEquals(GateAgent.ENTRY, note.entry());
        assertEquals("字数：2\n首字：主\n这是一句回答。", note.text());
        assertEquals(1, tools.size());
        assertEquals(GateAgent.ENTRY, tools.getFirst().getDefinition().getName());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("主题"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("countChars"));
    }
}
