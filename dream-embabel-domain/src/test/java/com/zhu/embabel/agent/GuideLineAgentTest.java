package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.GuideLine;
import com.zhu.embabel.knowledge.StyleGuide;
import org.junit.jupiter.api.Test;

class GuideLineAgentTest {

    @Test
    void attachesTheStyleGuideAndWritesFromTheFakeModel() {
        StyleGuide guide = new StyleGuide();
        try {
            FakeOperationContext context = FakeOperationContext.create();
            context.expectResponse("春草发芽，评论要先写出判断。");

            GuideLine line = new GuideLineAgent(guide).write(new UserInput("写一句关于春草的评论"), context.ai());

            assertEquals(StyleGuide.NAME, line.reference());
            assertEquals("春草发芽，评论要先写出判断。", line.text());
            var call = context.getLlmInvocations().get(0);
            assertEquals(Method.CREATE_OBJECT, call.getMethod());
            assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
            assertTrue(call.getPrompt().contains("写一句关于春草的评论"));
            assertTrue(call.getPrompt().contains("检索工具"));
            assertTrue(call.getInteraction().getTools().stream()
                    .anyMatch(tool -> "styleguide_textSearch".equals(tool.getDefinition().getName())));
            assertTrue(guide.reference().tools().stream()
                    .map(tool -> tool.getDefinition().getName())
                    .toList()
                    .containsAll(java.util.List.of(
                            "styleguide_textSearch",
                            "styleguide_vectorSearch",
                            "styleguide_broadenChunk",
                            "styleguide_zoomOut")));
        } finally {
            guide.close();
        }
    }
}
