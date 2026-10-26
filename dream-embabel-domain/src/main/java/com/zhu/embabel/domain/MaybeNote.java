package com.zhu.embabel.domain;

/**
 * createObjectIfPossible 的结果。found 为 false 表示这次没有抽出对象。
 */
public record MaybeNote(boolean found, String title, String body) {
}
