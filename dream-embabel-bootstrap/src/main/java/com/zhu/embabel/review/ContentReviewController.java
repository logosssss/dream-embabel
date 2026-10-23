package com.zhu.embabel.review;

import com.zhu.embabel.model.LocalLlmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交主题，拿回定稿。确认仍由 shell 的 x 接收。
 */
@RestController
@RequestMapping({"/api/reviews", "/api/reviews/"})
public class ContentReviewController {

    private final ContentReviewService reviews;

    public ContentReviewController(ContentReviewService reviews) {
        this.reviews = reviews;
    }

    @PostMapping
    public ResponseEntity<ContentReviewResponse> review(@RequestBody ContentReviewRequest request) {
        ContentReviewResponse response = reviews.review(request.topic());
        HttpStatus status = switch (response.status()) {
            case "COMPLETED" -> HttpStatus.OK;
            case "WAITING" -> HttpStatus.ACCEPTED;
            default -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ContentReviewResponse> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ContentReviewResponse.failed("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ContentReviewResponse> unavailable(IllegalStateException exception) {
        if (LocalLlmService.MISSING.equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ContentReviewResponse.failed("UNAVAILABLE", exception.getMessage()));
        }
        return ResponseEntity.internalServerError()
                .body(ContentReviewResponse.failed("FAILED", exception.getMessage()));
    }

    public record ContentReviewRequest(String topic) {
    }
}
