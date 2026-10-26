package com.zhu.embabel.domain;

/**
 * 监督者收成的结果。stage 是它实际调用过的准备动作。
 */
public record SupervisorNote(String stage, String text) {
}
