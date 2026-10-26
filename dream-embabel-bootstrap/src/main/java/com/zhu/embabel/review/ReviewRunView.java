package com.zhu.embabel.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 一次审稿的进度。页面在等待时轮询它。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewRunView(
        String runId,
        String status,
        List<ReviewStep> steps,
        String processId,
        String title,
        String body,
        String content,
        String message) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReviewStep(String name, String state, String message) {
    }
}
