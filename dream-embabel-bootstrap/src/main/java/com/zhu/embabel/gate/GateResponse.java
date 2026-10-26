package com.zhu.embabel.gate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.GateNote;

/**
 * 剧本门禁的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GateResponse(
        String processId,
        String status,
        String entry,
        String tools,
        String text,
        String message) {

    static GateResponse completed(String processId, GateNote note, String tools) {
        return new GateResponse(processId, "COMPLETED", note.entry(), tools, note.text(), null);
    }

    static GateResponse unfinished(String processId, String status, String message) {
        return new GateResponse(processId, status, null, null, null, message);
    }

}
