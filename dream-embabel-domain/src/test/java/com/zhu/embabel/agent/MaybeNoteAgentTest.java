package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.zhu.embabel.domain.Extracted;
import com.zhu.embabel.domain.MaybeNote;
import org.junit.jupiter.api.Test;

class MaybeNoteAgentTest {

    private final MaybeNoteAgent agent = new MaybeNoteAgent();

    @Test
    void extractKeepsTheObjectWhenOneIsReturned() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse(new Extracted("河边", "春草发芽。"));

        MaybeNote note = agent.extract(new UserInput("写春天的河边"), context.ai());

        assertTrue(note.found());
        assertEquals("河边", note.title());
        assertEquals("春草发芽。", note.body());
        assertEquals(Method.CREATE_OBJECT_IF_POSSIBLE, context.getLlmInvocations().get(0).getMethod());
    }

    @Test
    void extractRecordsAMissWhenNothingComesBack() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse(null);

        MaybeNote note = agent.extract(new UserInput("抽不出"), context.ai());

        assertFalse(note.found());
        assertNull(note.title());
        assertNull(note.body());
        assertEquals(Method.CREATE_OBJECT_IF_POSSIBLE, context.getLlmInvocations().get(0).getMethod());
    }
}
