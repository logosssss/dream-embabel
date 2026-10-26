package com.zhu.embabel.domain;

/**
 * 一稿。低分那稿会被藏起来，重规划后的及格稿才留下。
 */
public record TrialDraft(int attempt, int score, int discardedScore, String discardedText, String text) {
}
