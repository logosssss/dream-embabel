package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.model.Chunk;
import com.zhu.embabel.domain.UrtextLine;
import com.zhu.embabel.knowledge.StyleGuide;
import java.util.List;

/**
 * 入库时标题被写进片段正文。模型只看 urtext，也就是去掉标题前缀的原文。
 */
@Agent(description = "按栏目规范原文写一句")
public class UrtextAgent {

    public static final String COLUMN = "春草";

    public static final String REVIEW = "判断";

    public static final String DISPATCH = "通讯";

    private final StyleGuide guide;

    public UrtextAgent(StyleGuide guide) {
        this.guide = guide;
    }

    @AchievesGoal(description = "按原文写一句")
    @Action(description = "只把原文交给模型，写一句")
    public UrtextLine write(UserInput input, Ai ai) {
        Chunk chunk = original(guide.chunks(), input.getContent());
        String urtext = chunk.getUrtext();
        String text = ai.withDefaultLlm().generateText(Prompts.topicThen("""
                下面是栏目规范的原文，没有标题前缀。
                只用一句中文回应主题，用到这段原文。
                """, input.getContent(), """
                # 原文
                %s
                """.formatted(urtext)));
        return new UrtextLine(chunk.getText(), urtext, Texts.required(text));
    }

    static Chunk original(List<Chunk> chunks, String topic) {
        String needle = needle(topic);
        for (Chunk chunk : chunks) {
            String urtext = chunk.getUrtext();
            if (urtext != null && urtext.contains(needle)) {
                return chunk;
            }
        }
        throw new IllegalStateException("没有对上的原文。");
    }

    static String needle(String topic) {
        if (topic != null && topic.contains(DISPATCH)) {
            return DISPATCH;
        }
        if (topic != null && topic.contains("评论")) {
            return REVIEW;
        }
        return COLUMN;
    }

}
