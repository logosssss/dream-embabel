package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.common.workflow.control.ResultList;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.ConsensusNote;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsensusAgentTest {

    private final ConsensusAgent agent = new ConsensusAgent();

    @Test
    void matchingAnswersAgreeAndDifferentAnswersStaySplit() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("是\nJava 是一门具体的编程语言。");
        context.expectResponse("否\n这句话只给了标题，没有具体内容。");
        UserInput input = new UserInput("用一句话介绍 Java");

        ConsensusNote cool = agent.vote(input, context.ai(), ConsensusAgent.COOL, ConsensusAgent.COOL_TEMPERATURE);
        ConsensusNote warm = agent.vote(input, context.ai(), ConsensusAgent.WARM, ConsensusAgent.WARM_TEMPERATURE);
        ConsensusNote agreed = ConsensusAgent.join(new ResultList<>(List.of(
                new ConsensusNote(null, null, "是\nJava 是一门具体的编程语言。", null),
                new ConsensusNote(null, null, null, "是\n它在介绍一门语言。"))));
        ConsensusNote split = ConsensusAgent.join(new ResultList<>(List.of(warm, cool)));

        assertEquals("是\nJava 是一门具体的编程语言。", cool.cool());
        assertNull(cool.warm());
        assertEquals("否\n这句话只给了标题，没有具体内容。", warm.warm());
        assertEquals(ConsensusAgent.AGREED, agreed.verdict());
        assertEquals("是", agreed.agreed());
        assertEquals(ConsensusAgent.SPLIT, split.verdict());
        assertNull(split.agreed());
        assertEquals(0.0, context.getLlmInvocations().get(0).getInteraction().getLlm().getTemperature());
        assertEquals(0.8, context.getLlmInvocations().get(1).getInteraction().getLlm().getTemperature());
        assertCall(context, 0);
        assertCall(context, 1);
    }

    private static void assertCall(FakeOperationContext context, int index) {
        var call = context.getLlmInvocations().get(index);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains("第一行只写「是」或「否」"));
        assertTrue(call.getPrompt().contains("不要照抄主题原句"));
    }
}
