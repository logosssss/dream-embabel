package com.zhu.embabel.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.SimpleLine;

/**
 * 简式构建器写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimpleLineResponse(String processId, String status, String text, String message) {

    static SimpleLineResponse completed(String processId, SimpleLine line) {
        return new SimpleLineResponse(processId, "COMPLETED", line.text(), null);
    }

    static SimpleLineResponse unfinished(String processId, String status, String message) {
        return new SimpleLineResponse(processId, status, null, message);
    }

}
