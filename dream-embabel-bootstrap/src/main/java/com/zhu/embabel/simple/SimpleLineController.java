package com.zhu.embabel.simple;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回简式构建器写出的一句。
 */
@RestController
@RequestMapping({"/api/simple-lines", "/api/simple-lines/"})
public class SimpleLineController {

    private final SimpleLineService lines;

    public SimpleLineController(SimpleLineService lines) {
        this.lines = lines;
    }

    @PostMapping
    public ResponseEntity<SimpleLineResponse> write(@RequestBody SimpleLineRequest request) {
        SimpleLineResponse response = lines.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record SimpleLineRequest(String text) {
    }
}
