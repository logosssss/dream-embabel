package com.zhu.embabel.domain;

/**
 * generateText 返回的一段文字。正文由这次纯文本调用填入，不是结构化抽取。
 */
public record TextLine(String text) {
}
