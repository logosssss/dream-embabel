package com.zhu.embabel.stamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.StampNote;

/**
 * fromInstance 盖章的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StampResponse(
        String processId,
        String status,
        String tool,
        String tools,
        String text,
        String message) {

    static StampResponse completed(String processId, StampNote note, String tools) {
        return new StampResponse(processId, "COMPLETED", note.tool(), tools, note.text(), null);
    }

    static StampResponse unfinished(String processId, String status, String message) {
        return new StampResponse(processId, status, null, null, null, message);
    }

}
