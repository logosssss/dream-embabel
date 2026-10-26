package com.zhu.embabel.unfold;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.UnfoldNote;

/**
 * 渐进披露的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnfoldResponse(
        String processId,
        String status,
        String entry,
        String tools,
        String text,
        String message) {

    static UnfoldResponse completed(String processId, UnfoldNote note, String tools) {
        return new UnfoldResponse(processId, "COMPLETED", note.entry(), tools, note.text(), null);
    }

    static UnfoldResponse unfinished(String processId, String status, String message) {
        return new UnfoldResponse(processId, status, null, null, null, message);
    }

}
