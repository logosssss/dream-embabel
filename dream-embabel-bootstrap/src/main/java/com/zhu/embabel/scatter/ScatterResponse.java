package com.zhu.embabel.scatter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.ScatterNote;

/**
 * 分发合成的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScatterResponse(String processId, String status, String benefit, String cost, String message) {

    static ScatterResponse completed(String processId, ScatterNote note) {
        return new ScatterResponse(processId, "COMPLETED", note.benefit(), note.cost(), null);
    }

    static ScatterResponse unfinished(String processId, String status, String message) {
        return new ScatterResponse(processId, status, null, null, message);
    }

}
