package com.zhu.embabel.maths;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.MathAnswer;

/**
 * 数学工具组的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MathGroupResponse(
        String processId,
        String status,
        String group,
        String tools,
        String text,
        String message) {

    static MathGroupResponse completed(String processId, MathAnswer answer, String tools) {
        return new MathGroupResponse(processId, "COMPLETED", answer.group(), tools, answer.text(), null);
    }

    static MathGroupResponse unfinished(String processId, String status, String message) {
        return new MathGroupResponse(processId, status, null, null, null, message);
    }

}
