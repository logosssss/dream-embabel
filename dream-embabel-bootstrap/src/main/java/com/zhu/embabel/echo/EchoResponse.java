package com.zhu.embabel.echo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.EchoLine;

/**
 * 调用 echo 后写一句的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EchoResponse(
        String processId,
        String status,
        String tool,
        String tools,
        String text,
        String message) {

    static EchoResponse completed(String processId, EchoLine line, String tools) {
        return new EchoResponse(processId, "COMPLETED", line.tool(), tools, line.text(), null);
    }

    static EchoResponse unfinished(String processId, String status, String message) {
        return new EchoResponse(processId, status, null, null, null, message);
    }

}
