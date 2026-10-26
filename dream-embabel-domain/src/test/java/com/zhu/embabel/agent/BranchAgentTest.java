package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.core.Action;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.core.Condition;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.plan.common.condition.ConditionDetermination;
import com.zhu.embabel.domain.BranchNote;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class BranchAgentTest {

    private final BranchAgent agent = new BranchAgent();

    @Test
    void conditionGuardsWhichActionCanRun() {
        AgentScope scope = new AgentMetadataReader().createAgentMetadata(agent);
        Set<String> names = scope.getConditions().stream().map(Condition::getName).collect(Collectors.toSet());

        assertTrue(names.contains(BranchAgent.CODE));
        assertTrue(names.contains(BranchAgent.PLAIN));
        assertEquals(ConditionDetermination.TRUE, pre(scope, BranchAgent.CODE).get(BranchAgent.CODE));
        assertEquals(ConditionDetermination.TRUE, pre(scope, BranchAgent.PLAIN).get(BranchAgent.PLAIN));
    }

    @Test
    void chosenBranchCallsTheModelAboutTheTopic() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("这段代码还没说清输入是什么。");
        context.expectResponse("河边的春天适合写成一句景色。");

        BranchNote code = agent.codeNote(new UserInput("请审这段代码"), context.ai());
        BranchNote plain = agent.plainNote(new UserInput("写春天的河边"), context.ai());

        assertEquals("code", code.branch());
        assertEquals("这段代码还没说清输入是什么。", code.text());
        assertEquals("plain", plain.branch());
        assertEquals("河边的春天适合写成一句景色。", plain.text());
        assertTrue(agent.mentionsCode(new UserInput("review this code")));
        assertTrue(agent.plainTopic(new UserInput("写春天的河边")));
        assertCall(context, 0, "请审这段代码", "代码问题");
        assertCall(context, 1, "写春天的河边", "只用一句中文回应");
    }

    private static void assertCall(FakeOperationContext context, int index, String topic, String job) {
        var call = context.getLlmInvocations().get(index);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains(topic));
        assertTrue(call.getPrompt().contains(job));
        assertTrue(call.getPrompt().contains("不要照抄主题原句"));
    }

    private static java.util.Map<String, ConditionDetermination> pre(AgentScope scope, String name) {
        Action action = scope.getActions().stream()
                .filter(candidate -> candidate.getPreconditions().containsKey(name))
                .findFirst()
                .orElseThrow();
        return action.getPreconditions();
    }
}
