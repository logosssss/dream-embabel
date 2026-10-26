package com.zhu.embabel.domain;

/**
 * 按标签从记忆里取出的一条事实，以及模型据此写的一句。
 */
public record MemoryLine(String label, String name, String fact, String text) {
}
