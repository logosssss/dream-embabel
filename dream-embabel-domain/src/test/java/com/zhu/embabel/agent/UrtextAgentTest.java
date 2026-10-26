package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.UrtextLine;
import com.zhu.embabel.knowledge.StyleGuide;
import org.junit.jupiter.api.Test;

class UrtextAgentTest {

    @Test
    void modelSeesTheOriginalTextWithoutTheTitlePrefix() {
        StyleGuide guide = new StyleGuide();
        try {
            UrtextAgent agent = new UrtextAgent(guide);
            FakeOperationContext context = FakeOperationContext.create();
            context.expectResponse("春草发芽，专栏要标题点题。");
            UserInput input = new UserInput("用原文写一句关于春草");

            UrtextLine line = agent.write(input, context.ai());

            assertTrue(line.enhanced().contains("# Title:"), line.enhanced());
            assertFalse(line.urtext().contains("# Title:"), line.urtext());
            assertTrue(line.urtext().contains("春草发芽"), line.urtext());
            assertEquals("春草发芽，专栏要标题点题。", line.text());
            var call = context.getLlmInvocations().get(0);
            assertEquals(Method.CREATE_OBJECT, call.getMethod());
            assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
            assertTrue(call.getPrompt().contains("用原文写一句关于春草"));
            assertTrue(call.getPrompt().contains(line.urtext()));
            assertFalse(call.getPrompt().contains("# Title:"));
        } finally {
            guide.close();
        }
    }
}
