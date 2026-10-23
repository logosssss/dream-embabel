package com.zhu.embabel.domain;

/**
 * 最近一次审校摘要。下一轮初稿会读到它。
 */
public record ReviewNote(String issues, String suggestions, int score) {

    public static ReviewNote from(Review review) {
        return new ReviewNote(review.issues(), review.suggestions(), review.score());
    }
}
