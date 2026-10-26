package com.zhu.embabel.domain;

/**
 * 模型尝试抽出的标题和正文。抽不到时这个类型不会出现。
 */
public record Extracted(String title, String body) {
}
