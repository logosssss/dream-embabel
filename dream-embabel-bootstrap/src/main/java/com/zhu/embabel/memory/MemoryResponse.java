package com.zhu.embabel.memory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.MemoryLine;

/**
 * 按标签取出记忆后写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemoryResponse(
        String processId,
        String status,
        String label,
        String name,
        String fact,
        String text,
        String message) {

    static MemoryResponse completed(String processId, MemoryLine line) {
        return new MemoryResponse(
                processId, "COMPLETED", line.label(), line.name(), line.fact(), line.text(), null);
    }

    static MemoryResponse unfinished(String processId, String status, String message) {
        return new MemoryResponse(processId, status, null, null, null, null, message);
    }

}
