package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.Review;
import org.junit.jupiter.api.Test;

class ContentReviewAgentTest {

    private final ContentReviewAgent agent = new ContentReviewAgent(120, 40, 80);

    @Test
    void draftContentAsksForChineseDraftAtCreativeTemperature() {
        FakeOperationContext context = FakeOperationContext.create();
        Draft expected = new Draft("春", "春草");
        context.expectResponse(expected);

        Draft actual = agent.draftContent(new UserInput("写春天"), context.ai());

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("写一篇中文初稿"));
        assertTrue(call.getPrompt().contains("120"));
        assertTrue(call.getPrompt().contains("写春天"));
        assertEquals(0.7, call.getInteraction().getLlm().getTemperature());
    }

    @Test
    void reviewDraftAsksForScoreAtLowTemperature() {
        FakeOperationContext context = FakeOperationContext.create();
        Review expected = new Review("跑题", "收回主题", 60);
        context.expectResponse(expected);

        Review actual = agent.reviewDraft(
                new UserInput("写春天"),
                new Draft("夏", "夏天很长"),
                context.ai());

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("审校下面的中文初稿"));
        assertTrue(call.getPrompt().contains("40"));
        assertTrue(call.getPrompt().contains("夏天很长"));
        assertEquals(0.2, call.getInteraction().getLlm().getTemperature());
    }

    @Test
    void finalizeUsesAutoLlmAndReviewNotes() {
        FakeOperationContext context = FakeOperationContext.create();
        FinalArticle expected = new FinalArticle("春", "春草发芽");
        context.expectResponse(expected);

        FinalArticle actual = agent.finalize(
                new UserInput("写春天"),
                new Draft("夏", "夏天很长"),
                new Review("跑题", "改回春天", 40),
                context.ai());

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("改写成中文定稿"));
        assertTrue(call.getPrompt().contains("80"));
        assertTrue(call.getPrompt().contains("改回春天"));
        assertTrue(call.getPrompt().contains("40"));
        assertNull(call.getInteraction().getLlm().getTemperature());
    }

    private static LlmInvocation onlyCall(FakeOperationContext context) {
        assertEquals(1, context.getLlmInvocations().size());
        return context.getLlmInvocations().get(0);
    }
}
