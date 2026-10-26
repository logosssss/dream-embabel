package com.zhu.embabel.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.UtilityChoice;

/**
 * Utility 规划的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UtilityResponse(
        String processId,
        String status,
        String choice,
        Integer askValue,
        Integer answerValue,
        String text,
        String message) {

    static UtilityResponse completed(String processId, UtilityChoice choice) {
        return new UtilityResponse(
                processId,
                "COMPLETED",
                choice.choice(),
                choice.askValue(),
                choice.answerValue(),
                choice.text(),
                null);
    }

    static UtilityResponse unfinished(String processId, String status, String message) {
        return new UtilityResponse(processId, status, null, null, null, null, message);
    }

}
