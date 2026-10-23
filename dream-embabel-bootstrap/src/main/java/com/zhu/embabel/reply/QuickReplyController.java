package com.zhu.embabel.reply;

import com.zhu.embabel.model.LocalLlmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 一句话进，一句短回答出。
 */
@RestController
@RequestMapping({"/api/replies", "/api/replies/"})
public class QuickReplyController {

    private final QuickReplyService replies;

    public QuickReplyController(QuickReplyService replies) {
        this.replies = replies;
    }

    @PostMapping
    public ResponseEntity<QuickReplyResponse> reply(@RequestBody QuickReplyRequest request) {
        QuickReplyResponse response = replies.reply(request.message());
        HttpStatus status = "COMPLETED".equals(response.status()) ? HttpStatus.OK : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<QuickReplyResponse> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(QuickReplyResponse.failed("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<QuickReplyResponse> unavailable(IllegalStateException exception) {
        if (LocalLlmService.MISSING.equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(QuickReplyResponse.failed("UNAVAILABLE", exception.getMessage()));
        }
        return ResponseEntity.internalServerError()
                .body(QuickReplyResponse.failed("FAILED", exception.getMessage()));
    }

    public record QuickReplyRequest(String message) {
    }
}
