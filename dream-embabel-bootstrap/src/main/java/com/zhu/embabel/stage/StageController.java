package com.zhu.embabel.stage;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回监督者分派后的成稿。
 */
@RestController
@RequestMapping({"/api/stages", "/api/stages/"})
public class StageController {

    private final StageService stages;

    public StageController(StageService stages) {
        this.stages = stages;
    }

    @PostMapping
    public ResponseEntity<StageResponse> run(@RequestBody StageRequest request) {
        StageResponse response = stages.run(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record StageRequest(String text) {
    }
}
