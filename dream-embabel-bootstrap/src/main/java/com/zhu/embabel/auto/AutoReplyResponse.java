package com.zhu.embabel.auto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.AutoNote;

/**
 * 自动选模型的 HTTP 结果。selection 固定是 auto，不由模型填写。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AutoReplyResponse(
        String processId, String status, String selection, String text, String message) {

    static AutoReplyResponse completed(String processId, AutoNote note) {
        return new AutoReplyResponse(processId, "COMPLETED", "auto", note.text(), null);
    }

    static AutoReplyResponse unfinished(String processId, String status, String message) {
        return new AutoReplyResponse(processId, status, "auto", null, message);
    }

}
