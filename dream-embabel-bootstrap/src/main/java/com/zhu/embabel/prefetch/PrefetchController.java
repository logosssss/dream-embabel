package com.zhu.embabel.prefetch;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回预取资料和模型写的一句。
 */
@RestController
@RequestMapping({"/api/prefetches", "/api/prefetches/"})
public class PrefetchController {

    private final PrefetchService prefetches;

    public PrefetchController(PrefetchService prefetches) {
        this.prefetches = prefetches;
    }

    @PostMapping
    public ResponseEntity<PrefetchResponse> write(@RequestBody PrefetchRequest request) {
        PrefetchResponse response = prefetches.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record PrefetchRequest(String text) {
    }
}
