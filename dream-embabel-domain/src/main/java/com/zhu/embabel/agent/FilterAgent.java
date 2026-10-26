package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.filter.PropertyFilter;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import com.zhu.embabel.domain.FilterLine;
import com.zhu.embabel.knowledge.StyleGuide;
import java.util.List;

/**
 * 检索前按章节标题过滤。模型看不到这条过滤，也绕不过去。
 */
@Agent(description = "按章节过滤栏目规范后再写一句")
public class FilterAgent {

    public static final String SECTION = "leaf_section_title";

    public static final String COLUMN = "专栏";

    public static final String REVIEW = "评论";

    public static final String DISPATCH = "通讯";

    public static final int TOP_K = 4;

    private final StyleGuide guide;

    public FilterAgent(StyleGuide guide) {
        this.guide = guide;
    }

    @AchievesGoal(description = "按章节写一句")
    @Action(description = "先按章节过滤，再写一句")
    public FilterLine write(UserInput input, Ai ai) {
        String section = sectionFor(input.getContent());
        ToolishRag reference = prepared(input.getContent());
        String text = ai.withDefaultLlm().withReference(reference).generateText(Prompts.topic("""
                用户主题如下。引用里只剩下「%s」这一节，其它章节已经被滤掉。
                只用一句中文回应，用到留下来的内容。
                """.formatted(section), input.getContent()));
        return new FilterLine(section, hintOf(reference), Texts.required(text));
    }

    /** 主题里出现的章节名决定留下哪一节。都没有就留专栏。 */
    static String sectionFor(String topic) {
        if (topic != null && topic.contains(DISPATCH)) {
            return DISPATCH;
        }
        if (topic != null && topic.contains(REVIEW)) {
            return REVIEW;
        }
        return COLUMN;
    }

    ToolishRag prepared(String topic) {
        return guide.reference()
                .withMetadataFilter(new PropertyFilter.Eq(SECTION, sectionFor(topic)))
                .withEagerSearchAbout(TextSimilaritySearchRequest.create(topic, 0.0, TOP_K));
    }

    static String hintOf(ToolishRag reference) {
        List<PromptContributor> hints = reference.getHints();
        if (hints == null || hints.isEmpty()) {
            throw new IllegalStateException("过滤后没有资料。");
        }
        String text = hints.get(hints.size() - 1).contribution();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("过滤后没有资料。");
        }
        return text.trim();
    }

}
