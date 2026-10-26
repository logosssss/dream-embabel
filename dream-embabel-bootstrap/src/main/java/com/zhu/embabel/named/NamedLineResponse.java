package com.zhu.embabel.named;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.NamedLine;

/**
 * 带调用 id 的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NamedLineResponse(
        String processId,
        String status,
        String callId,
        String personality,
        String text,
        String message) {

    static NamedLineResponse completed(String processId, NamedLine line, String personality) {
        return new NamedLineResponse(processId, "COMPLETED", line.callId(), personality, line.text(), null);
    }

    static NamedLineResponse unfinished(String processId, String status, String message) {
        return new NamedLineResponse(processId, status, null, null, null, message);
    }

}
