package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.core.Action;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.core.support.BlackboardWorldState;
import com.embabel.agent.api.tool.TerminateAgentException;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.plan.Plan;
import com.embabel.plan.common.condition.ConditionAction;
import com.embabel.plan.common.condition.ConditionDetermination;
import com.embabel.plan.common.condition.ConditionGoal;
import com.embabel.plan.common.condition.ConditionPlanningSystem;
import com.embabel.plan.common.condition.ConditionWorldState;
import com.embabel.plan.common.condition.WorldStateDeterminer;
import com.embabel.plan.utility.UtilityPlanner;
import com.zhu.embabel.domain.UtilityChoice;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UtilityAgentTest {

    private final UtilityAgent agent = new UtilityAgent();

    @Test
    void urgentTopicMakesAskingTheHigherUtility() {
        UserInput urgent = new UserInput("这件事很急");
        UserInput calm = new UserInput("写一段说明");

        assertEquals(UtilityAgent.HIGH, agent.askValue(urgent));
        assertEquals(UtilityAgent.LOW, agent.answerValue(urgent));
        assertEquals(UtilityAgent.LOW, agent.askValue(calm));
        assertEquals(UtilityAgent.HIGH, agent.answerValue(calm));
        assertEquals("ask", chosen(scope(), urgent));
        assertEquals("answer", chosen(scope(), calm));
    }

    @Test
    void chosenActionWritesWithTheDefaultModel() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("你要先解决哪一件？");
        assertThrows(TerminateAgentException.class, () -> agent.ask(new UserInput("这件事很急"), context.ai(), context));
        UtilityChoice ask = context.last(UtilityChoice.class);

        assertEquals("ask", ask.choice());
        assertEquals("你要先解决哪一件？", ask.text());
        assertEquals(Method.CREATE_OBJECT, context.getLlmInvocations().get(0).getMethod());
        assertInstanceOf(
                DefaultModelSelectionCriteria.class,
                context.getLlmInvocations().get(0).getInteraction().getLlm().getCriteria());
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("这件事很急"));
        assertTrue(context.getLlmInvocations().get(0).getPrompt().contains("只追问"));

        context.expectResponse("Java 是一门跨平台的编程语言。");
        assertThrows(
                TerminateAgentException.class,
                () -> agent.answer(new UserInput("用一句话介绍 Java"), context.ai(), context));
        UtilityChoice answer = context.last(UtilityChoice.class);

        assertEquals("answer", answer.choice());
        assertEquals("Java 是一门跨平台的编程语言。", answer.text());
        assertTrue(context.getLlmInvocations().get(1).getPrompt().contains("用一句话介绍 Java"));
        assertTrue(context.getLlmInvocations().get(1).getPrompt().contains("直接用一句中文回答"));
    }

    private static AgentScope scope() {
        return new AgentMetadataReader().createAgentMetadata(new UtilityAgent());
    }

    private static String chosen(AgentScope scope, UserInput input) {
        FakeOperationContext board = FakeOperationContext.create();
        board.addObject(input);
        Map<String, ConditionDetermination> known = new HashMap<>();
        for (Action action : scope.getActions()) {
            action.getPreconditions().forEach(known::put);
        }
        BlackboardWorldState state = new BlackboardWorldState(
                ConditionWorldState.Companion.invoke(known), board);
        UtilityPlanner planner = new UtilityPlanner(new WorldStateDeterminer() {
            @Override
            public ConditionWorldState determineWorldState() {
                return state;
            }

            @Override
            public ConditionDetermination determineCondition(String name) {
                return known.getOrDefault(name, ConditionDetermination.UNKNOWN);
            }
        });
        Set<ConditionAction> actions = new HashSet<>(scope.getActions());
        Set<ConditionGoal> goals = new HashSet<>(scope.getGoals());
        Plan plan = planner.bestValuePlanToAnyGoal(new ConditionPlanningSystem(actions, goals));
        assertNotNull(plan, known.toString());
        String name = plan.getActions().get(0).getName();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(dot + 1);
    }
}
