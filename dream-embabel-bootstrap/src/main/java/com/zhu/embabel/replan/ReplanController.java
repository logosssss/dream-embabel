package com.zhu.embabel.replan;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回低分重规划后的及格稿。
 */
@RestController
@RequestMapping({"/api/replans", "/api/replans/"})
public class ReplanController {

    private final ReplanService replans;

    public ReplanController(ReplanService replans) {
        this.replans = replans;
    }

    @PostMapping
    public ResponseEntity<ReplanResponse> replan(@RequestBody ReplanRequest request) {
        ReplanResponse response = replans.replan(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record ReplanRequest(String text) {
    }
}
