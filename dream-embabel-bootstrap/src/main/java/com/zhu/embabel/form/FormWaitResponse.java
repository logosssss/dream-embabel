package com.zhu.embabel.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 表单停住或写完一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FormWaitResponse(
        String processId,
        String status,
        String title,
        List<FormField> fields,
        String text,
        String message) {

    static FormWaitResponse waiting(String processId, String title, List<FormField> fields) {
        return new FormWaitResponse(processId, "WAITING", title, fields, null, null);
    }

    static FormWaitResponse completed(String processId, String text) {
        return new FormWaitResponse(processId, "COMPLETED", null, null, text, null);
    }

    static FormWaitResponse unfinished(String processId, String status, String message) {
        return new FormWaitResponse(processId, status, null, null, null, message);
    }

    public record FormField(String id, String label) {
    }
}
