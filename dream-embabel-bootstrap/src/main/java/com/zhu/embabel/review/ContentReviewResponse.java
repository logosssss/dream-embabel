package com.zhu.embabel.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.FinalArticle;

/**
 * HTTP 返回给定稿页面的结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentReviewResponse(
        String processId,
        String status,
        String title,
        String body,
        String content,
        String message) {

    static ContentReviewResponse completed(String processId, FinalArticle article) {
        return new ContentReviewResponse(
                processId,
                "COMPLETED",
                article.title(),
                article.body(),
                article.getContent(),
                null);
    }

    static ContentReviewResponse waiting(String processId, String message, String title, String body) {
        return new ContentReviewResponse(processId, "WAITING", title, body, null, message);
    }

    static ContentReviewResponse unfinished(String processId, String status, String message) {
        return new ContentReviewResponse(processId, status, null, null, null, message);
    }

}
