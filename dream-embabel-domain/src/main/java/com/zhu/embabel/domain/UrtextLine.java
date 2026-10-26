package com.zhu.embabel.domain;

/**
 * 对照增强后的片段和原文后写出的一句。text 是模型写的。
 */
public record UrtextLine(String enhanced, String urtext, String text) {
}
