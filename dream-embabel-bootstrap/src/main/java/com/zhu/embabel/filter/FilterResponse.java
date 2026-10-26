package com.zhu.embabel.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.FilterLine;

/**
 * 按章节过滤后写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilterResponse(
        String processId, String status, String section, String hint, String text, String message) {

    static FilterResponse completed(String processId, FilterLine line) {
        return new FilterResponse(processId, "COMPLETED", line.section(), line.hint(), line.text(), null);
    }

    static FilterResponse unfinished(String processId, String status, String message) {
        return new FilterResponse(processId, status, null, null, null, message);
    }

}
