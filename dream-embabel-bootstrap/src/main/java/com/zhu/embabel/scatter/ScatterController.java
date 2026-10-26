package com.zhu.embabel.scatter;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回好处和代价两句。
 */
@RestController
@RequestMapping({"/api/scatters", "/api/scatters/"})
public class ScatterController {

    private final ScatterService scatters;

    public ScatterController(ScatterService scatters) {
        this.scatters = scatters;
    }

    @PostMapping
    public ResponseEntity<ScatterResponse> write(@RequestBody ScatterRequest request) {
        ScatterResponse response = scatters.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record ScatterRequest(String text) {
    }
}
