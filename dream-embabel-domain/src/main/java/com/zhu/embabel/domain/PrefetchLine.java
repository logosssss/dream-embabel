package com.zhu.embabel.domain;

/**
 * 预取后写出的一句。hint 是开口前检索到的资料，text 是模型写的。
 */
public record PrefetchLine(String hint, String text) {
}
