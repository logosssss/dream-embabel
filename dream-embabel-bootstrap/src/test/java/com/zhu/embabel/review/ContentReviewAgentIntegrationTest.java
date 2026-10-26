package com.zhu.embabel.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.embabel.agent.test.integration.EmbabelMockitoIntegrationTest;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.support.LlmInteraction;
import com.embabel.common.ai.model.ByRoleModelSelectionCriteria;
import com.zhu.embabel.knowledge.HouseStyleTools;
import com.zhu.embabel.model.ModelProfileEnvironmentPostProcessor;
import com.embabel.common.ai.model.ModelType;
import com.zhu.embabel.agent.ContentReviewAgent;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.QuickReply;
import com.zhu.embabel.domain.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 基类把默认模型设成 test-model，并 mock 掉 LlmOperations。
 * 这里补一个同名模型，避免零模型时框架拒绝启动。创作、审校、定稿改写和定稿打分都是 creating，不打 generateText。
 */
@Import(ContentReviewAgentIntegrationTest.TestModel.class)
class ContentReviewAgentIntegrationTest extends EmbabelMockitoIntegrationTest {

    static {
        System.setProperty(ModelProfileEnvironmentPostProcessor.AUTO_PROFILE, "false");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void reviewsAndFinalizesWithoutCallingAModel() {
        UserInput input = new UserInput("写春天");
        FinalArticle article = stubReview();

        FinalArticle result = AgentInvocation.create(agentPlatform, FinalArticle.class).invoke(input);

        assertEquals(article, result);
        assertTrue(result.getContent().contains("春草发芽"));

        verifyCreateObjectMatching(
                prompt -> prompt.contains("写一篇中文初稿") && prompt.contains("400"),
                Draft.class,
                llm -> roleIs(llm, ContentReviewAgent.WRITER_ROLE)
                        && Double.valueOf(0.7).equals(llm.getLlm().getTemperature())
                        && offersHouseStyle(llm)
                        && offersTextSearch(llm)
                        && offersChunkNavigation(llm));
        verifyCreateObjectMatching(
                prompt -> prompt.contains("审校下面的中文初稿") && prompt.contains("200"),
                Review.class,
                llm -> roleIs(llm, ContentReviewAgent.REVIEWER_ROLE)
                        && Double.valueOf(0.2).equals(llm.getLlm().getTemperature())
                        && !offersHouseStyle(llm)
                        && offersTextSearch(llm)
                        && offersChunkNavigation(llm));
        verifyCreateObjectMatching(
                prompt -> prompt.contains("改写成中文定稿") && prompt.contains("500"),
                FinalArticle.class,
                llm -> roleIs(llm, ContentReviewAgent.WRITER_ROLE)
                        && llm.getLlm().getTemperature() == null
                        && offersHouseStyle(llm)
                        && offersTextSearch(llm)
                        && offersChunkNavigation(llm));
        verifyCreateObjectMatching(
                prompt -> prompt.contains("给这篇定稿打分") && prompt.contains("0.60"),
                TextFeedback.class,
                llm -> roleIs(llm, ContentReviewAgent.REVIEWER_ROLE)
                        && Double.valueOf(0.2).equals(llm.getLlm().getTemperature())
                        && !offersHouseStyle(llm)
                        && offersTextSearch(llm)
                        && offersChunkNavigation(llm));
        verifyNoMoreInteractions();
    }

    @Test
    void reviewsOverHttpWithoutCallingAModel() throws Exception {
        stubReview();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"写春天\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.title").value("春"))
                .andExpect(jsonPath("$.body").value("春草发芽"))
                .andExpect(jsonPath("$.content").value(containsString("春草发芽")));
    }

    @Test
    void repliesOverHttpWithOneModelCall() throws Exception {
        whenCreateObject(prompt -> prompt.contains("用一两句中文直接回答"), QuickReply.class)
                .thenReturn(new QuickReply("春天来了。"));

        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"用一句话介绍春天\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.reply").value("春天来了。"));

        verifyCreateObjectMatching(
                prompt -> prompt.contains("用一两句中文直接回答") && prompt.contains("用一句话介绍春天"),
                QuickReply.class,
                llm -> llm.getTools().isEmpty()
                        && llm.getGuardRails().isEmpty()
                        && Double.valueOf(0.2).equals(llm.getLlm().getTemperature())
                        && Integer.valueOf(256).equals(llm.getLlm().getMaxTokens()));
        verifyNoMoreInteractions();
    }

    @Test
    void rejectsBlankReply() throws Exception {
        mockMvc.perform(post("/api/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void rejectsBlankTopic() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    private FinalArticle stubReview() {
        Draft draft = new Draft("春", "春草");
        Review review = new Review("略散", "收紧段落", 70);
        FinalArticle article = new FinalArticle("春", "春草发芽");
        whenCreateObject(prompt -> prompt.contains("写一篇中文初稿"), Draft.class).thenReturn(draft);
        whenCreateObject(prompt -> prompt.contains("审校下面的中文初稿"), Review.class).thenReturn(review);
        whenCreateObject(prompt -> prompt.contains("改写成中文定稿"), FinalArticle.class).thenReturn(article);
        whenCreateObject(prompt -> prompt.contains("给这篇定稿打分"), TextFeedback.class)
                .thenReturn(new TextFeedback(0.8, "可以定稿"));
        return article;
    }

    @TestConfiguration
    static class TestModel {

        @Bean
        @SuppressWarnings("unchecked")
        LlmService<?> testModel() {
            LlmService<?> service = mock(LlmService.class);
            when(service.getName()).thenReturn("test-model");
            when(service.getProvider()).thenReturn("test");
            when(service.getType()).thenReturn(ModelType.LLM);
            return service;
        }
    }

    private static boolean offersTextSearch(LlmInteraction llm) {
        return offersTool(llm, "styleguide_textSearch");
    }

    private static boolean offersChunkNavigation(LlmInteraction llm) {
        return offersTool(llm, "styleguide_broadenChunk") && offersTool(llm, "styleguide_zoomOut");
    }

    private static boolean offersTool(LlmInteraction llm, String name) {
        return llm.getTools().stream()
                .map(Tool::getDefinition)
                .anyMatch(definition -> name.equals(definition.getName()));
    }

    private static boolean offersHouseStyle(LlmInteraction llm) {
        return llm.getTools().stream()
                .map(Tool::getDefinition)
                .anyMatch(definition -> HouseStyleTools.NAME.equals(definition.getName()));
    }

    private static boolean roleIs(LlmInteraction llm, String role) {
        return llm.getLlm().getModelSelectionCriteria() instanceof ByRoleModelSelectionCriteria criteria
                && role.equals(criteria.getRole());
    }
}
