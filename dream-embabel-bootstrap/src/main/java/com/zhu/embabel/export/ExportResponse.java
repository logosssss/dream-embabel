package com.zhu.embabel.export;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 导出目标写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExportResponse(
        String processId,
        String status,
        String name,
        Boolean remote,
        String text,
        String message) {

    static ExportResponse completed(String processId, String name, boolean remote, String text) {
        return new ExportResponse(processId, "COMPLETED", name, remote, text, null);
    }

    static ExportResponse unfinished(String processId, String status, String message) {
        return new ExportResponse(processId, status, null, null, null, message);
    }

}
