package com.zhu.embabel.prefetch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.PrefetchLine;

/**
 * 预取后写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrefetchResponse(String processId, String status, String hint, String text, String message) {

    static PrefetchResponse completed(String processId, PrefetchLine line) {
        return new PrefetchResponse(processId, "COMPLETED", line.hint(), line.text(), null);
    }

    static PrefetchResponse unfinished(String processId, String status, String message) {
        return new PrefetchResponse(processId, status, null, null, message);
    }

}
