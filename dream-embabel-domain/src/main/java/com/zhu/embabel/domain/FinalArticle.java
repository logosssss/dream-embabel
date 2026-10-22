package com.zhu.embabel.domain;

import com.embabel.agent.domain.library.HasContent;
import com.embabel.common.core.types.Timestamped;
import java.time.Instant;
import tools.jackson.annotation.JsonIgnore;

/**
 * 定稿。时间戳和正文展示不交给模型填，避免进入结构化输出的字段。
 */
public record FinalArticle(String title, String body) implements HasContent, Timestamped {

    @Override
    @JsonIgnore
    public Instant getTimestamp() {
        return Instant.now();
    }

    @Override
    @JsonIgnore
    public String getContent() {
        return """
                # %s

                %s
                """.formatted(title, body).trim();
    }
}
