package com.zhu.embabel.guide;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.GuideLine;

/**
 * 按规范写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GuideLineResponse(
        String processId, String status, String reference, String tools, String text, String message) {

    static GuideLineResponse completed(String processId, GuideLine line, String tools) {
        return new GuideLineResponse(processId, "COMPLETED", line.reference(), tools, line.text(), null);
    }

    static GuideLineResponse unfinished(String processId, String status, String message) {
        return new GuideLineResponse(processId, status, null, null, null, message);
    }

}
