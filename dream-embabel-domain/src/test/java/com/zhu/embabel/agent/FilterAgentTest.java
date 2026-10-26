package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.filter.PropertyFilter;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.zhu.embabel.domain.FilterLine;
import com.zhu.embabel.knowledge.StyleGuide;
import org.junit.jupiter.api.Test;

class FilterAgentTest {

    @Test
    void reviewTopicKeepsOnlyTheReviewSection() {
        StyleGuide guide = new StyleGuide();
        try {
            FilterAgent agent = new FilterAgent(guide);
            FakeOperationContext context = FakeOperationContext.create();
            context.expectResponse("评论要先写出判断，再给依据。");
            UserInput input = new UserInput("按评论的规范写一句关于春草");

            ToolishRag prepared = agent.prepared(input.getContent());
            FilterLine line = agent.write(input, context.ai());

            PropertyFilter.Eq filter = assertInstanceOf(PropertyFilter.Eq.class, prepared.getMetadataFilter());
            assertEquals(FilterAgent.SECTION, filter.getKey());
            assertEquals(FilterAgent.REVIEW, filter.getValue());
            assertTrue(line.hint().contains("判断"), line.hint());
            assertFalse(line.hint().contains("春草发芽"), line.hint());
            assertFalse(line.hint().contains("按时间写清"), line.hint());
            assertEquals(FilterAgent.REVIEW, line.section());
            assertEquals("评论要先写出判断，再给依据。", line.text());
            var call = context.getLlmInvocations().get(0);
            assertEquals(Method.CREATE_OBJECT, call.getMethod());
            assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
            assertTrue(call.getPrompt().contains("按评论的规范写一句关于春草"));
            assertTrue(call.getPrompt().contains("评论"));
        } finally {
            guide.close();
        }
    }
}
