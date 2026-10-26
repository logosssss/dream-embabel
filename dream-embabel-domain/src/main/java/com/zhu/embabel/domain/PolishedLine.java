package com.zhu.embabel.domain;

/**
 * 反思循环里的一轮句子。循环自己拿它去打分，页面不直接返回这一层。
 */
public record PolishedLine(String text) {
}
