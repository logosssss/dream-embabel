package com.zhu.embabel.domain;

/**
 * fromInstance 盖章之后的一句。tool 是挂上的工具名。
 */
public record StampNote(String tool, String text) {
}
