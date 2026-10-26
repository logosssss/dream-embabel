package com.zhu.embabel.reflect;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回反思循环的两轮句子。
 */
@RestController
@RequestMapping({"/api/reflections", "/api/reflections/"})
public class ReflectiveController {

    private final ReflectiveService reflections;

    public ReflectiveController(ReflectiveService reflections) {
        this.reflections = reflections;
    }

    @PostMapping
    public ResponseEntity<ReflectiveResponse> run(@RequestBody ReflectiveRequest request) {
        ReflectiveResponse response = reflections.run(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record ReflectiveRequest(String text) {
    }
}
