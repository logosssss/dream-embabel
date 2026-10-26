package com.zhu.embabel.domain;

/**
 * 按顺序选模型后写出的一句。first 是先找的名字，second 是后面那个。
 */
public record FallbackLine(String first, String second, String text) {
}
