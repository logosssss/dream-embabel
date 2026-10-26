package com.zhu.embabel.text;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一句话，拿回 generateText 的一段文字。
 */
@RestController
@RequestMapping({"/api/text-lines", "/api/text-lines/"})
public class TextLineController {

    private final TextLineService lines;

    public TextLineController(TextLineService lines) {
        this.lines = lines;
    }

    @PostMapping
    public ResponseEntity<TextLineResponse> line(@RequestBody TextLineRequest request) {
        TextLineResponse response = lines.line(request.message());
        return RunStatus.respond(response.status(), response);
    }

    public record TextLineRequest(String message) {
    }
}
