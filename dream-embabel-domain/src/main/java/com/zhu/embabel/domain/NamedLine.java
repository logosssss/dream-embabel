package com.zhu.embabel.domain;

/**
 * 一次带调用 id 的模型结果。personality 由配置读出，不写死在这句话里。
 */
public record NamedLine(String callId, String text) {
}
