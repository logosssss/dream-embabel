package com.zhu.embabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.embabel.agent.test.integration.EmbabelMockitoIntegrationTest;
import com.embabel.common.ai.model.ModelType;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.Review;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 基类把默认模型设成 test-model，并 mock 掉 LlmOperations。
 * 这里补一个同名模型，避免零模型时框架拒绝启动。三个动作都是 creating，不打 generateText。
 */
@Import(ContentReviewAgentIntegrationTest.TestModel.class)
class ContentReviewAgentIntegrationTest extends EmbabelMockitoIntegrationTest {

    @Test
    void reviewsAndFinalizesWithoutCallingAModel() {
        UserInput input = new UserInput("写春天");
        Draft draft = new Draft("春", "春草");
        Review review = new Review("略散", "收紧段落", 70);
        FinalArticle article = new FinalArticle("春", "春草发芽");

        whenCreateObject(prompt -> prompt.contains("写一篇中文初稿"), Draft.class).thenReturn(draft);
        whenCreateObject(prompt -> prompt.contains("审校下面的中文初稿"), Review.class).thenReturn(review);
        whenCreateObject(prompt -> prompt.contains("改写成中文定稿"), FinalArticle.class).thenReturn(article);

        FinalArticle result = AgentInvocation.create(agentPlatform, FinalArticle.class).invoke(input);

        assertEquals(article, result);
        assertTrue(result.getContent().contains("春草发芽"));

        verifyCreateObjectMatching(
                prompt -> prompt.contains("写一篇中文初稿") && prompt.contains("400"),
                Draft.class,
                llm -> Double.valueOf(0.7).equals(llm.getLlm().getTemperature()));
        verifyCreateObjectMatching(
                prompt -> prompt.contains("审校下面的中文初稿") && prompt.contains("200"),
                Review.class,
                llm -> Double.valueOf(0.2).equals(llm.getLlm().getTemperature()));
        verifyCreateObjectMatching(
                prompt -> prompt.contains("改写成中文定稿") && prompt.contains("500"),
                FinalArticle.class,
                llm -> llm.getLlm().getTemperature() == null);
        verifyNoMoreInteractions();
    }

    @TestConfiguration
    static class TestModel {

        @Bean
        @SuppressWarnings("unchecked")
        LlmService<LlmService<?>> testModel() {
            LlmService<LlmService<?>> service = mock(LlmService.class);
            when(service.getName()).thenReturn("test-model");
            when(service.getProvider()).thenReturn("test");
            when(service.getType()).thenReturn(ModelType.LLM);
            return service;
        }
    }
}
