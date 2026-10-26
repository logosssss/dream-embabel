package com.zhu.embabel.domain;

/**
 * 两路合成后的结果。好处和代价各来自一次模型调用。
 */
public record ScatterNote(String benefit, String cost) {
}
