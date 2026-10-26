package com.zhu.embabel.model;

/**
 * 当前注册的对话模型全是占位。页面应返回 503。
 */
public class ModelUnavailableException extends IllegalStateException {

    public ModelUnavailableException(String message) {
        super(message);
    }
}
