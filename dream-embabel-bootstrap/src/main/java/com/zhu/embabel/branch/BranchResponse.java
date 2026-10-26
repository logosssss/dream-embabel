package com.zhu.embabel.branch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.BranchNote;

/**
 * 条件分支的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BranchResponse(String processId, String status, String branch, String text, String message) {

    static BranchResponse completed(String processId, BranchNote note) {
        return new BranchResponse(processId, "COMPLETED", note.branch(), note.text(), null);
    }

    static BranchResponse unfinished(String processId, String status, String message) {
        return new BranchResponse(processId, status, null, null, message);
    }

}
