package com.zhu.embabel.reply;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.QuickReply;

/**
 * 短回答的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuickReplyResponse(String processId, String status, String reply, String message) {

    static QuickReplyResponse completed(String processId, QuickReply reply) {
        return new QuickReplyResponse(processId, "COMPLETED", reply.reply(), null);
    }

    static QuickReplyResponse unfinished(String processId, String status, String message) {
        return new QuickReplyResponse(processId, status, null, message);
    }

    static QuickReplyResponse failed(String status, String message) {
        return new QuickReplyResponse(null, status, null, message);
    }
}
