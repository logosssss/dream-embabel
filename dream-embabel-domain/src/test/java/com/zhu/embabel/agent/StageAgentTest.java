package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.api.annotation.support.SupervisorAction;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.Brief;
import com.zhu.embabel.domain.SupervisorNote;
import org.junit.jupiter.api.Test;

class StageAgentTest {

    private final StageAgent agent = new StageAgent();

    @Test
    void supervisorKeepsASingleGoalAction() {
        AgentScope scope = new AgentMetadataReader().createAgentMetadata(agent);

        assertEquals(1, scope.getGoals().size());
        assertEquals(1, scope.getActions().size());
        assertInstanceOf(SupervisorAction.class, scope.getActions().get(0));
    }

    @Test
    void prepareWritesWithTheDefaultModelAndFinishKeepsThatSentence() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 适合一次编写、到处运行。");

        Brief brief = agent.prepare(new UserInput("用一句话介绍 Java"), context.ai());
        SupervisorNote note = agent.finish(brief);

        assertEquals("Java 适合一次编写、到处运行。", note.text());
        assertEquals(StageAgent.STAGE, note.stage());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("准备稿"));
    }
}
