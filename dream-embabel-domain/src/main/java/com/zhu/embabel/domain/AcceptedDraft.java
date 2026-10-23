package com.zhu.embabel.domain;

/**
 * 已确认的初稿。审校读它。未打开确认时，由初稿直接转换。
 */
public record AcceptedDraft(String title, String body) {
}
