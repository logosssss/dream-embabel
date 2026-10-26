package com.zhu.embabel.domain;

/**
 * 低分重规划之后的及格稿。discardedScore 是被丢掉的那一稿。
 */
public record ReplanNote(int attempt, int score, int discardedScore, String discardedText, String text) {
}
