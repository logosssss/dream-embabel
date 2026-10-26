package com.zhu.embabel.domain;

/**
 * 条件分支的结果。branch 是规划选中的那一支，不是模型填的。
 */
public record BranchNote(String branch, String text) {
}
