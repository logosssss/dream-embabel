package com.zhu.embabel.http;

import com.zhu.embabel.agent.EchoUnavailableException;
import com.zhu.embabel.model.ModelUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 按异常类型决定状态码。不再比较异常里的中文句子。
 */
@RestControllerAdvice
public class AgentHttpAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiFailure> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiFailure("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler({ModelUnavailableException.class, EchoUnavailableException.class})
    public ResponseEntity<ApiFailure> unavailable(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiFailure("UNAVAILABLE", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiFailure> failed(IllegalStateException exception) {
        return ResponseEntity.internalServerError().body(new ApiFailure("FAILED", exception.getMessage()));
    }
}
