package com.zhu.embabel.domain;

/**
 * 审校结果。score 为 0 到 100。
 */
public record Review(String issues, String suggestions, int score) {
}
