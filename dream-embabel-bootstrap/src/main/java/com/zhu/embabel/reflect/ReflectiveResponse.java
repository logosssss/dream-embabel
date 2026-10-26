package com.zhu.embabel.reflect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.ReflectiveNote;

/**
 * 反思循环的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReflectiveResponse(
        String processId,
        String status,
        Integer attempts,
        Double score,
        Double discardedScore,
        String discardedText,
        String text,
        String message) {

    static ReflectiveResponse completed(String processId, ReflectiveNote note) {
        return new ReflectiveResponse(
                processId,
                "COMPLETED",
                note.attempts(),
                note.score(),
                note.discardedText() == null ? null : note.discardedScore(),
                note.discardedText(),
                note.text(),
                null);
    }

    static ReflectiveResponse unfinished(String processId, String status, String message) {
        return new ReflectiveResponse(processId, status, null, null, null, null, null, message);
    }

}
