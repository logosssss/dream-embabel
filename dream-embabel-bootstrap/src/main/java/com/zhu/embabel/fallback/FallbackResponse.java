package com.zhu.embabel.fallback;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.FallbackLine;

/**
 * 按顺序选模型的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FallbackResponse(
        String processId, String status, String first, String second, String text, String message) {

    static FallbackResponse completed(String processId, FallbackLine line) {
        return new FallbackResponse(processId, "COMPLETED", line.first(), line.second(), line.text(), null);
    }

    static FallbackResponse unfinished(String processId, String status, String message) {
        return new FallbackResponse(processId, status, null, null, null, message);
    }

}
