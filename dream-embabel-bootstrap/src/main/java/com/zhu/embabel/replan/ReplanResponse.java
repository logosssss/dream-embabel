package com.zhu.embabel.replan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.ReplanNote;

/**
 * 低分重规划的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReplanResponse(
        String processId,
        String status,
        Integer attempt,
        Integer score,
        Integer discardedScore,
        String discardedText,
        String text,
        String message) {

    static ReplanResponse completed(String processId, ReplanNote note) {
        return new ReplanResponse(
                processId,
                "COMPLETED",
                note.attempt(),
                note.score(),
                note.discardedScore(),
                note.discardedText(),
                note.text(),
                null);
    }

    static ReplanResponse unfinished(String processId, String status, String message) {
        return new ReplanResponse(processId, status, null, null, null, null, null, message);
    }

}
