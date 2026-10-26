package com.zhu.embabel.fallback;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回按顺序选出的模型写的一句。
 */
@RestController
@RequestMapping({"/api/fallbacks", "/api/fallbacks/"})
public class FallbackController {

    private final FallbackService fallbacks;

    public FallbackController(FallbackService fallbacks) {
        this.fallbacks = fallbacks;
    }

    @PostMapping
    public ResponseEntity<FallbackResponse> write(@RequestBody FallbackRequest request) {
        FallbackResponse response = fallbacks.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record FallbackRequest(String text) {
    }
}
