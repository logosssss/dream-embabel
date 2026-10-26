package com.zhu.embabel.subagent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.SubagentLine;

/**
 * 子代理转述的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubagentResponse(
        String processId,
        String status,
        String tool,
        String tools,
        String text,
        String message) {

    static SubagentResponse completed(String processId, SubagentLine line, String tools) {
        return new SubagentResponse(processId, "COMPLETED", line.tool(), tools, line.text(), null);
    }

    static SubagentResponse unfinished(String processId, String status, String message) {
        return new SubagentResponse(processId, status, null, null, null, message);
    }

}
