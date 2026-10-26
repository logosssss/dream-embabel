package com.zhu.embabel.domain;

/**
 * 两路判断合成的结果。verdict 由代码比较，不是模型填的。
 */
public record ConsensusNote(String verdict, String agreed, String cool, String warm) {
}
