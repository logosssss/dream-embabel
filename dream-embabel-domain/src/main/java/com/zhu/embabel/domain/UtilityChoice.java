package com.zhu.embabel.domain;

/**
 * Utility 规划器选中的那一支。分数是两个动作当时的效用。text 是选中后再调用模型写成的正文。
 */
public record UtilityChoice(String choice, int askValue, int answerValue, String text) {
}
