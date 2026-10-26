package com.zhu.embabel.domain;

/**
 * 反思循环的结果。第一轮没过线时，discardedText 是被丢掉的那一句。
 */
public record ReflectiveNote(
        int attempts,
        double score,
        double discardedScore,
        String discardedText,
        String text) {
}
