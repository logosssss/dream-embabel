package com.zhu.embabel.domain;

/**
 * 低分重规划留下的标记。有它，规划器才会选择重写，而不是再交回第一次的低分稿。
 */
public record RetryMark(int score, String text) {
}
