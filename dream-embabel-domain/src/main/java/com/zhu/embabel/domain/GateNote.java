package com.zhu.embabel.domain;

/**
 * 按剧本走完之后的一句。entry 是剧本工具名。
 */
public record GateNote(String entry, String text) {
}
