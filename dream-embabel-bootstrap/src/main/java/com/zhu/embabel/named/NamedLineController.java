package com.zhu.embabel.named;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回带调用 id 的一句，以及当前日志人格。
 */
@RestController
@RequestMapping({"/api/named-lines", "/api/named-lines/"})
public class NamedLineController {

    private final NamedLineService lines;

    public NamedLineController(NamedLineService lines) {
        this.lines = lines;
    }

    @PostMapping
    public ResponseEntity<NamedLineResponse> write(@RequestBody NamedLineRequest request) {
        NamedLineResponse response = lines.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record NamedLineRequest(String text) {
    }
}
