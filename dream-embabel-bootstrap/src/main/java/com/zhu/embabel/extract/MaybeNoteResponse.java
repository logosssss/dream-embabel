package com.zhu.embabel.extract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.MaybeNote;

/**
 * 可空抽取的 HTTP 结果。found 为 false 时没有标题和正文。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MaybeNoteResponse(
        String processId, String status, Boolean found, String title, String body, String message) {

    static MaybeNoteResponse completed(String processId, MaybeNote note) {
        return new MaybeNoteResponse(processId, "COMPLETED", note.found(), note.title(), note.body(), null);
    }

    static MaybeNoteResponse unfinished(String processId, String status, String message) {
        return new MaybeNoteResponse(processId, status, null, null, null, message);
    }

}
