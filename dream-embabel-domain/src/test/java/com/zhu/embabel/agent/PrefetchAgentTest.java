package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.PrefetchLine;
import com.zhu.embabel.knowledge.StyleGuide;
import org.junit.jupiter.api.Test;

class PrefetchAgentTest {

    @Test
    void prefetchesTheTopicBeforeTheFakeModelWrites() {
        StyleGuide guide = new StyleGuide();
        try {
            PrefetchAgent agent = new PrefetchAgent(guide);
            FakeOperationContext context = FakeOperationContext.create();
            context.expectResponse("春草发芽，评论要先写出判断。");
            UserInput input = new UserInput("写一句关于春草的评论");

            ToolishRag prepared = agent.prepared(input.getContent());
            PrefetchLine line = agent.write(input, context.ai());

            assertTrue(PrefetchAgent.hintOf(prepared).contains("写一句关于春草的评论"));
            assertEquals(PrefetchAgent.hintOf(prepared), line.hint());
            assertEquals("春草发芽，评论要先写出判断。", line.text());
            var call = context.getLlmInvocations().get(0);
            assertEquals(Method.CREATE_OBJECT, call.getMethod());
            assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
            assertTrue(call.getPrompt().contains("写一句关于春草的评论"));
            assertTrue(call.getPrompt().contains("预取"));
            assertTrue(call.getInteraction().getTools().stream()
                    .anyMatch(tool -> "styleguide_textSearch".equals(tool.getDefinition().getName())));
        } finally {
            guide.close();
        }
    }
}
