package com.zhu.embabel.agent;

/**
 * 模型返回的正文。空白视为这次调用失败。
 */
public final class Texts {

    private Texts() {
    }

    public static String required(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("模型没有返回正文。");
        }
        return text.trim();
    }
}
