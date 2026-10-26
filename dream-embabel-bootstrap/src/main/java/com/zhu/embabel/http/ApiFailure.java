package com.zhu.embabel.http;

/**
 * 各演示页共用的失败体。页面只读 status 和 message。
 */
public record ApiFailure(String status, String message) {
}
