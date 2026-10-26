package com.zhu.embabel.stage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.SupervisorNote;

/**
 * Supervisor 规划的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StageResponse(
        String processId,
        String status,
        String stage,
        String text,
        String message) {

    static StageResponse completed(String processId, SupervisorNote note) {
        return new StageResponse(processId, "COMPLETED", note.stage(), note.text(), null);
    }

    static StageResponse unfinished(String processId, String status, String message) {
        return new StageResponse(processId, status, null, null, message);
    }

}
