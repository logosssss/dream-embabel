package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.core.Goal;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.ExportLine;
import org.junit.jupiter.api.Test;

class ExportLineAgentTest {

    @Test
    void goalIsExportedFromUserInputAndTheModelWritesOneSentence() {
        AgentScope scope = new AgentMetadataReader().createAgentMetadata(new ExportLineAgent());
        Goal goal = scope.getGoals().stream()
                .filter(item -> ExportLineAgent.EXPORTED.equals(item.getExport().getName()))
                .findFirst()
                .orElseThrow();

        assertTrue(goal.getExport().getRemote());
        assertTrue(goal.getExport().getLocal());
        assertTrue(goal.getExport().getStartingInputTypes().contains(UserInput.class));

        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 可以在不同系统上跑。");
        ExportLine line = new ExportLineAgent().write(new UserInput("用一句话介绍 Java"), context.ai());

        assertEquals("Java 可以在不同系统上跑。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains("只用一句中文回应"));
    }
}
