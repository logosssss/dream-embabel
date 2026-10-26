package com.zhu.embabel.memory;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回按标签取出的事实和模型写的一句。
 */
@RestController
@RequestMapping({"/api/memories", "/api/memories/"})
public class MemoryController {

    private final MemoryService memories;

    public MemoryController(MemoryService memories) {
        this.memories = memories;
    }

    @PostMapping
    public ResponseEntity<MemoryResponse> write(@RequestBody MemoryRequest request) {
        MemoryResponse response = memories.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record MemoryRequest(String text) {
    }
}
