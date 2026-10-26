package com.zhu.embabel.domain;

/**
 * 先调用 echo 再写出的一句。tool 是挂上的工具名。
 */
public record EchoLine(String tool, String text) {
}
