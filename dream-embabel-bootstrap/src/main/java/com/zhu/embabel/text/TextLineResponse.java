package com.zhu.embabel.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.TextLine;

/**
 * 纯文本的 HTTP 结果。text 来自 generateText 的字符串。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextLineResponse(String processId, String status, String text, String message) {

    static TextLineResponse completed(String processId, TextLine line) {
        return new TextLineResponse(processId, "COMPLETED", line.text(), null);
    }

    static TextLineResponse unfinished(String processId, String status, String message) {
        return new TextLineResponse(processId, status, null, message);
    }

}
