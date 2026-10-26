package com.zhu.embabel.domain;

/**
 * 按章节过滤后写出的一句。section 是留下的章节，hint 是过滤后的资料。
 */
public record FilterLine(String section, String hint, String text) {
}
