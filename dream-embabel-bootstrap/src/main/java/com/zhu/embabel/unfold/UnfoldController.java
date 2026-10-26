package com.zhu.embabel.unfold;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回先展开目录再写的一句。
 */
@RestController
@RequestMapping({"/api/unfolds", "/api/unfolds/"})
public class UnfoldController {

    private final UnfoldService unfolds;

    public UnfoldController(UnfoldService unfolds) {
        this.unfolds = unfolds;
    }

    @PostMapping
    public ResponseEntity<UnfoldResponse> write(@RequestBody UnfoldRequest request) {
        UnfoldResponse response = unfolds.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record UnfoldRequest(String text) {
    }
}
