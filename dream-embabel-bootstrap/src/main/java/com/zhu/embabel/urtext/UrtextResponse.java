package com.zhu.embabel.urtext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.UrtextLine;

/**
 * 对照原文后写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UrtextResponse(
        String processId,
        String status,
        String enhanced,
        String urtext,
        String text,
        String message) {

    static UrtextResponse completed(String processId, UrtextLine line) {
        return new UrtextResponse(processId, "COMPLETED", line.enhanced(), line.urtext(), line.text(), null);
    }

    static UrtextResponse unfinished(String processId, String status, String message) {
        return new UrtextResponse(processId, status, null, null, null, message);
    }

}
