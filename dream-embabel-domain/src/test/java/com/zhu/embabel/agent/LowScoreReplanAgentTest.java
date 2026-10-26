package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.core.ReplanRequestedException;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.ReplanNote;
import com.zhu.embabel.domain.RetryMark;
import com.zhu.embabel.domain.TrialDraft;
import org.junit.jupiter.api.Test;

class LowScoreReplanAgentTest {

    private final LowScoreReplanAgent agent = new LowScoreReplanAgent();

    @Test
    void lowScoreHidesTheDraftAndTheRewritePasses() {
        FakeOperationContext context = FakeOperationContext.create();
        UserInput input = new UserInput("java");
        context.expectResponse("Java 是一门");
        TrialDraft first = agent.firstDraft(input, context.ai());
        assertEquals("Java 是一门", first.text());
        assertPromptUsesTopic(context.getLlmInvocations().get(0), "java", "故意不要写完");

        ReplanRequestedException error = assertThrows(ReplanRequestedException.class, () -> agent.accept(first));

        assertTrue(error.getReason().contains("40"));
        assertTrue(error.getReason().contains("60"));
        context.addObject(first);
        error.getBlackboardUpdater().accept(context);
        assertTrue(context.objectsOfType(TrialDraft.class).isEmpty());
        RetryMark mark = context.objectsOfType(RetryMark.class).get(0);
        assertEquals(40, mark.score());
        assertEquals(first.text(), mark.text());

        context.expectResponse("Java 是一门跨平台的编程语言。");
        ReplanNote note = agent.accept(agent.rewrite(input, mark, context.ai()));

        assertEquals(2, note.attempt());
        assertEquals(90, note.score());
        assertEquals(40, note.discardedScore());
        assertEquals("Java 是一门", note.discardedText());
        assertEquals("Java 是一门跨平台的编程语言。", note.text());
        LlmInvocation second = context.getLlmInvocations().get(1);
        assertPromptUsesTopic(second, "java", "写成完整的一句");
        assertTrue(second.getPrompt().contains("Java 是一门"));
    }

    private static void assertPromptUsesTopic(LlmInvocation call, String topic, String instruction) {
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains(topic));
        assertTrue(call.getPrompt().contains(instruction));
    }
}
