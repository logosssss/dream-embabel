package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.core.Goal;
import com.embabel.agent.core.ReplanRequestedException;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import com.embabel.agent.tools.mcp.ToolCallContextMcpMetaConverter;
import com.embabel.agent.core.hitl.AwaitableResponseException;
import com.embabel.agent.core.support.LlmInteraction;
import com.embabel.agent.api.common.workflow.loop.TextFeedback;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.model.Chunk;
import com.embabel.agent.rag.tools.SearchDefaults;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.ByRoleModelSelectionCriteria;
import com.zhu.embabel.domain.AcceptedDraft;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.domain.Review;
import com.zhu.embabel.domain.ReviewNote;
import com.zhu.embabel.knowledge.HouseStyleTools;
import com.zhu.embabel.knowledge.StyleGuide;
import java.util.List;
import java.util.Set;
import kotlin.jvm.functions.Function1;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

class ContentReviewAgentTest {

    private final ReviewMemory memory = new ReviewMemory();
    private final HouseStyleTools houseStyle = new HouseStyleTools();
    private final StyleGuide styleGuide = new StyleGuide();
    private final TopicGuardRail topicGuard = new TopicGuardRail();
    private final ContentReviewAgent agent = new ContentReviewAgent(
            120, 40, 80, 60, false, houseStyle, styleGuide, topicGuard, null, null);

    @Test
    void draftContentAsksForChineseDraftAtCreativeTemperature() {
        FakeOperationContext context = FakeOperationContext.create();
        Draft expected = new Draft("春", "春草");
        context.expectResponse(expected);

        Draft actual = agent.draftContent(new UserInput("写春天"), memory, context.ai());

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("写一篇中文初稿"));
        assertTrue(call.getPrompt().contains("120"));
        assertTrue(call.getPrompt().contains("写春天"));
        assertFalse(call.getPrompt().contains("上一轮审校"));
        assertFalse(call.getPrompt().contains("远程工具"));
        assertTrue(call.getPrompt().contains("houseStyle"));
        assertTrue(call.getPrompt().contains("检索工具"));
        assertTrue(offersHouseStyle(call.getInteraction()));
        assertTrue(offersTextSearch(call.getInteraction()));
        assertTrue(offersChunkNavigation(call.getInteraction()));
        assertEquals(0.7, call.getInteraction().getLlm().getTemperature());
        assertEquals(ContentReviewAgent.WRITER_ROLE, roleOf(call));
    }

    @Test
    void reviewDraftAsksForScoreAtLowTemperature() {
        FakeOperationContext context = FakeOperationContext.create();
        Review expected = new Review("跑题", "收回主题", 60);
        context.expectResponse(expected);

        Review actual = agent.reviewDraft(
                new UserInput("写春天"),
                new Draft("夏", "夏天很长"),
                new AcceptedDraft("夏", "夏天很长"),
                memory,
                context.ai());

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("审校下面的中文初稿"));
        assertTrue(call.getPrompt().contains("40"));
        assertTrue(call.getPrompt().contains("夏天很长"));
        assertEquals(0.2, call.getInteraction().getLlm().getTemperature());
        assertEquals(ContentReviewAgent.REVIEWER_ROLE, roleOf(call));
        assertFalse(offersHouseStyle(call.getInteraction()));
        assertTrue(offersTextSearch(call.getInteraction()));
        assertTrue(offersChunkNavigation(call.getInteraction()));
    }

    @Test
    void lowScoreHidesTheDraftAndRequestsReplan() {
        FakeOperationContext context = FakeOperationContext.create();
        Draft draft = new Draft("夏", "夏天很长");
        AcceptedDraft accepted = new AcceptedDraft("夏", "夏天很长");
        context.addObject(draft);
        context.addObject(accepted);
        context.expectResponse(new Review("跑题", "重写", 59));

        ReplanRequestedException error = assertThrows(
                ReplanRequestedException.class,
                () -> agent.reviewDraft(new UserInput("写春天"), draft, accepted, memory, context.ai()));

        assertTrue(error.getReason().contains("59"));
        assertEquals(59, memory.latest().score());
        error.getBlackboardUpdater().accept(context);
        assertTrue(context.objectsOfType(Draft.class).isEmpty());
        assertTrue(context.objectsOfType(AcceptedDraft.class).isEmpty());
    }

    @Test
    void nextDraftIncludesTheRememberedReview() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse(new Draft("春", "春草"));
        memory.remember(new ReviewNote("跑题", "收紧开头", 59));

        agent.draftContent(new UserInput("写春天"), memory, context.ai());

        String prompt = onlyCall(context).getPrompt();
        assertTrue(prompt.contains("上一轮审校"));
        assertTrue(prompt.contains("收紧开头"));
        assertTrue(prompt.contains("59"));
    }

    @Test
    void acceptDraftPassesThroughWhenConfirmationIsOff() {
        Draft draft = new Draft("春", "春草");

        assertEquals(new AcceptedDraft("春", "春草"), agent.acceptDraft(draft));
    }

    @Test
    void acceptDraftWaitsForConfirmationWhenEnabled() {
        ContentReviewAgent confirming = new ContentReviewAgent(
                120, 40, 80, 60, true, houseStyle, styleGuide, topicGuard, null, null);
        Draft draft = new Draft("春", "春草");

        AwaitableResponseException error = assertThrows(
                AwaitableResponseException.class, () -> confirming.acceptDraft(draft));

        AcceptedDraft payload = assertInstanceOf(AcceptedDraft.class, error.getAwaitable().getPayload());
        assertEquals(new AcceptedDraft("春", "春草"), payload);
    }

    @Test
    void rewriteUsesWriterRoleAndThePreviousPolishNote() {
        FakeOperationContext context = FakeOperationContext.create();
        FinalArticle expected = new FinalArticle("春", "春草发芽");
        context.expectResponse(expected);

        FinalArticle actual = agent.rewrite(
                new UserInput("写春天"),
                new Draft("夏", "夏天很长"),
                new Review("跑题", "改回春天", 40),
                context.ai(),
                "还没有上一轮意见。");

        assertEquals(expected, actual);
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("改写成中文定稿"));
        assertTrue(call.getPrompt().contains("80"));
        assertTrue(call.getPrompt().contains("改回春天"));
        assertTrue(call.getPrompt().contains("40"));
        assertTrue(call.getPrompt().contains("还没有上一轮意见。"));
        assertNull(call.getInteraction().getLlm().getTemperature());
        assertEquals(ContentReviewAgent.WRITER_ROLE, roleOf(call));
        assertTrue(call.getPrompt().contains("houseStyle"));
        assertTrue(call.getPrompt().contains("检索工具"));
        assertTrue(offersHouseStyle(call.getInteraction()));
        assertTrue(offersTextSearch(call.getInteraction()));
        assertTrue(offersChunkNavigation(call.getInteraction()));
    }

    @Test
    void judgeScoresTheDraftOnAZeroToOneScale() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse(new TextFeedback(0.8, "可以定稿"));

        TextFeedback actual = agent.judge(
                new UserInput("写春天"),
                new Review("略散", "收紧段落", 70),
                new FinalArticle("春", "春草发芽"),
                context.ai());

        assertEquals(0.8, actual.getScore());
        assertEquals("可以定稿", actual.getFeedback());
        LlmInvocation call = onlyCall(context);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("给这篇定稿打分"));
        assertTrue(call.getPrompt().contains("0.60"));
        assertTrue(call.getPrompt().contains("春草发芽"));
        assertEquals(0.2, call.getInteraction().getLlm().getTemperature());
        assertEquals(ContentReviewAgent.REVIEWER_ROLE, roleOf(call));
        assertFalse(offersHouseStyle(call.getInteraction()));
        assertTrue(offersTextSearch(call.getInteraction()));
        assertTrue(offersChunkNavigation(call.getInteraction()));
    }

    @Test
    void remoteToolGroupIsMentionedWithoutCallingOut() {
        ContentReviewAgent withRemote = new ContentReviewAgent(
                120, 40, 80, 60, false, houseStyle, styleGuide, topicGuard, null, emptyRemoteGroup());
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse(new Draft("春", "春草"));

        withRemote.draftContent(new UserInput("写春天"), memory, context.ai());

        LlmInvocation call = onlyCall(context);
        assertTrue(call.getPrompt().contains("远程工具"));
        assertTrue(offersHouseStyle(call.getInteraction()));
        assertTrue(offersTextSearch(call.getInteraction()));
        assertTrue(offersChunkNavigation(call.getInteraction()));
    }

    @Test
    void exportedGoalStartsFromUserInput() {
        AgentScope scope = new AgentMetadataReader().createAgentMetadata(agent);
        Goal goal = scope.getGoals().stream()
                .filter(item -> ContentReviewAgent.EXPORTED_GOAL.equals(item.getExport().getName()))
                .findFirst()
                .orElseThrow();

        assertTrue(goal.getExport().getRemote());
        assertTrue(goal.getExport().getLocal());
        assertTrue(goal.getExport().getStartingInputTypes().contains(UserInput.class));
    }

    @Test
    void topicGuardRejectsABlankTopicWithoutAModel() {
        assertTrue(topicGuard.validate("写春天", null).isValid());
        assertTrue(topicGuard.validate("# 用户输入\n写春天\n", null).isValid());
        assertFalse(topicGuard.validate("  ", null).isValid());
        assertFalse(topicGuard.validate("# 用户输入\n\n# 标题\n夏\n", null).isValid());
        assertEquals("用户输入是空白。", topicGuard.validate((String) null, null).getErrors().get(0).getMessage());
    }

    @Test
    void styleGuideFindsTheClosestLocalNote() {
        assertEquals(
                SearchDefaults.DEFAULT_TEXT_SIMILARITY_THRESHOLD,
                styleGuide.reference().getSearchDefaults().getTextSimilarityThreshold());
        assertTrue(styleGuide.search("春草").contains("春草发芽"));
        assertTrue(styleGuide.search("评论").contains("判断"));
        assertEquals("没有可检索的资料。", styleGuide.search("  "));

        Chunk spring = styleGuide.chunks().stream()
                .filter(chunk -> chunk.getText().contains("春草发芽"))
                .findFirst()
                .orElseThrow();
        assertTrue(spring.getText().contains(StyleGuide.RESOURCE));
        assertFalse(styleGuide.zoom(spring.getId()).contains("No parent section found."));
        String broadened = styleGuide.broaden(spring.getId());
        assertTrue(broadened.contains("段落"));
    }

    @Test
    void houseStyleReturnsSectionRulesWithoutAModel() {
        assertTrue(houseStyle.houseStyle("评论").contains("判断"));
        assertTrue(houseStyle.houseStyle("通讯").contains("人物"));
        assertTrue(houseStyle.houseStyle("未知栏目").contains("短段落"));
        assertTrue(houseStyle.houseStyle(null).contains("短段落"));
    }

    private static McpToolGroup emptyRemoteGroup() {
        return new McpToolGroup(
                ToolGroupDescription.create("按配置的地址读取外部资料", "remote-notes"),
                "remote",
                "remote-notes",
                Set.of(ToolGroupPermission.INTERNET_ACCESS),
                List.of(),
                new Function1<ToolCallback, Boolean>() {
                    @Override
                    public Boolean invoke(ToolCallback callback) {
                        return Boolean.TRUE;
                    }
                },
                ToolCallContextMcpMetaConverter.noOp());
    }

    private static boolean offersTextSearch(LlmInteraction interaction) {
        return offersTool(interaction, "styleguide_textSearch");
    }

    private static boolean offersChunkNavigation(LlmInteraction interaction) {
        return offersTool(interaction, "styleguide_broadenChunk") && offersTool(interaction, "styleguide_zoomOut");
    }

    private static boolean offersTool(LlmInteraction interaction, String name) {
        return interaction.getTools().stream()
                .map(Tool::getDefinition)
                .anyMatch(definition -> name.equals(definition.getName()));
    }

    private static boolean offersHouseStyle(LlmInteraction interaction) {
        return interaction.getTools().stream()
                .map(Tool::getDefinition)
                .anyMatch(definition -> HouseStyleTools.NAME.equals(definition.getName()));
    }

    private static String roleOf(LlmInvocation call) {
        ByRoleModelSelectionCriteria criteria = assertInstanceOf(
                ByRoleModelSelectionCriteria.class,
                call.getInteraction().getLlm().getModelSelectionCriteria());
        return criteria.getRole();
    }

    private static LlmInvocation onlyCall(FakeOperationContext context) {
        assertEquals(1, context.getLlmInvocations().size());
        return context.getLlmInvocations().get(0);
    }
}
