package com.zhu.embabel.domain;

/**
 * 监督者分派出来的准备稿。成稿动作只收这一份，不再写新内容。
 */
public record Brief(String text) {
}
