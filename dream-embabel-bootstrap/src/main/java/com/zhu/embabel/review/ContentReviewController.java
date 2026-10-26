package com.zhu.embabel.review;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交主题，拿回定稿。等待确认时，页面也可以提交接受。
 */
@RestController
@RequestMapping({"/api/reviews", "/api/reviews/"})
public class ContentReviewController {

    private final ContentReviewService reviews;

    public ContentReviewController(ContentReviewService reviews) {
        this.reviews = reviews;
    }

    @PostMapping({"/runs", "/runs/"})
    public ResponseEntity<ReviewRunView> start(@RequestBody ContentReviewRequest request) {
        String runId = reviews.start(request.topic());
        return ResponseEntity.accepted().body(reviews.progress(runId));
    }

    @GetMapping({"/runs/{runId}", "/runs/{runId}/"})
    public ReviewRunView progress(@PathVariable String runId) {
        return reviews.progress(runId);
    }

    @PostMapping({"/runs/{runId}/confirmations", "/runs/{runId}/confirmations/"})
    public ResponseEntity<ReviewRunView> confirm(@PathVariable String runId) {
        reviews.confirm(runId);
        return ResponseEntity.accepted().body(reviews.progress(runId));
    }

    @PostMapping
    public ResponseEntity<ContentReviewResponse> review(@RequestBody ContentReviewRequest request) {
        ContentReviewResponse response = reviews.review(request.topic());
        return RunStatus.respond(response.status(), response);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ReviewRunView> missing(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ReviewRunView(null, "NOT_FOUND", List.of(), null, null, null, null, exception.getMessage()));
    }

    public record ContentReviewRequest(String topic) {
    }
}
