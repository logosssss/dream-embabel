package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.common.ai.prompt.PromptContributor;
import com.embabel.common.core.types.TextSimilaritySearchRequest;
import com.zhu.embabel.domain.PrefetchLine;
import com.zhu.embabel.knowledge.StyleGuide;
import java.util.List;

/**
 * 模型开口前先按主题做语义检索，把结果放进引用提示，再写一句。
 */
@Agent(description = "预取栏目规范后再写一句")
public class PrefetchAgent {

    public static final int TOP_K = 2;

    private final StyleGuide guide;

    public PrefetchAgent(StyleGuide guide) {
        this.guide = guide;
    }

    @AchievesGoal(description = "用预取资料写一句")
    @Action(description = "先预取栏目规范，再写一句")
    public PrefetchLine write(UserInput input, Ai ai) {
        ToolishRag reference = prepared(input.getContent());
        String text = ai.withDefaultLlm().withReference(reference).generateText(Prompts.topic("""
                用户主题如下。引用里已经有开口前预取的栏目规范。
                只用一句中文回应，用到预取到的内容。
                """, input.getContent()));
        return new PrefetchLine(hintOf(reference), Texts.required(text));
    }

    /** 按主题取最接近的两段。阈值放宽到 0，避免本地向量分数把结果滤空。 */
    ToolishRag prepared(String topic) {
        return guide.reference().withEagerSearchAbout(TextSimilaritySearchRequest.create(topic, 0.0, TOP_K));
    }

    static String hintOf(ToolishRag reference) {
        List<PromptContributor> hints = reference.getHints();
        if (hints == null || hints.isEmpty()) {
            throw new IllegalStateException("预取没有结果。");
        }
        String text = hints.get(hints.size() - 1).contribution();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("预取没有结果。");
        }
        return text.trim();
    }

}
