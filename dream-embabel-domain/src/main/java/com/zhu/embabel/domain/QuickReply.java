package com.zhu.embabel.domain;

/**
 * 一次模型调用的短回答。只有 reply 交给模型填。
 */
public record QuickReply(String reply) {
}
