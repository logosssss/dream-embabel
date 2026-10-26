package com.zhu.embabel.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 演示页按进程状态选 HTTP 状态码。各页仍返回自己的响应类型。
 */
public final class RunStatus {

    private RunStatus() {
    }

    public static HttpStatus of(String status) {
        if ("COMPLETED".equals(status)) {
            return HttpStatus.OK;
        }
        if ("WAITING".equals(status)) {
            return HttpStatus.ACCEPTED;
        }
        return HttpStatus.CONFLICT;
    }

    public static <T> ResponseEntity<T> respond(String status, T body) {
        return ResponseEntity.status(of(status)).body(body);
    }
}
