package com.zhu.embabel.consensus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.ConsensusNote;

/**
 * 两路共识的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConsensusResponse(
        String processId, String status, String verdict, String agreed, String cool, String warm, String message) {

    static ConsensusResponse completed(String processId, ConsensusNote note) {
        return new ConsensusResponse(
                processId, "COMPLETED", note.verdict(), note.agreed(), note.cool(), note.warm(), null);
    }

    static ConsensusResponse unfinished(String processId, String status, String message) {
        return new ConsensusResponse(processId, status, null, null, null, null, message);
    }

}
