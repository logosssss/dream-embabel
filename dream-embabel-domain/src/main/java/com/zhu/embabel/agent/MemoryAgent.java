package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.DataDictionary;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.rag.model.NamedEntityData;
import com.embabel.agent.rag.model.SimpleNamedEntityData;
import com.embabel.agent.rag.service.NamedEntityDataRepository;
import com.embabel.agent.rag.service.support.InMemoryNamedEntityDataRepository;
import com.zhu.embabel.domain.MemoryLine;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 长期事实是带标签的命名实体，按标签取出，不走向量检索。
 * 主题里有「评论」取忌讳，否则取术语。
 */
@Agent(description = "按标签取出一条记忆后再写一句")
public class MemoryAgent {

    public static final String TERM = "术语";

    public static final String AVOID = "忌讳";

    private final NamedEntityDataRepository memory;

    public MemoryAgent() {
        this.memory = new InMemoryNamedEntityDataRepository(
                DataDictionary.fromDomainTypes("glossary", List.of()));
        memory.save(entity(
                "term-spring",
                TERM,
                "春草",
                "专栏里的春草指刚发芽的草，标题要点题。"));
        memory.save(entity(
                "avoid-spring",
                AVOID,
                "春草",
                "评论里不要把春草写成景色抒情。"));
    }

    @AchievesGoal(description = "按记忆写一句")
    @Action(description = "按标签取出事实，再写一句")
    public MemoryLine write(UserInput input, Ai ai) {
        String label = labelFor(input.getContent());
        NamedEntityData fact = load(label);
        String text = ai.withDefaultLlm().generateText(Prompts.topicThen("""
                下面是按标签从记忆里取出的一条事实。
                只用一句中文回应主题，用到这条事实。
                """, input.getContent(), """
                # 标签
                %s

                # 事实
                %s
                """.formatted(label, fact.getDescription())));
        return new MemoryLine(label, fact.getName(), fact.getDescription(), Texts.required(text));
    }

    NamedEntityData load(String label) {
        List<NamedEntityData> found = memory.findByLabel(label);
        if (found == null || found.isEmpty()) {
            throw new IllegalStateException("没有这个标签的记忆：" + label);
        }
        return found.get(0);
    }

    static String labelFor(String topic) {
        if (topic != null && topic.contains("评论")) {
            return AVOID;
        }
        return TERM;
    }

    private static SimpleNamedEntityData entity(String id, String label, String name, String description) {
        return new SimpleNamedEntityData(id, null, name, description, Set.of(label), Map.of(), Map.of(), null);
    }

}
