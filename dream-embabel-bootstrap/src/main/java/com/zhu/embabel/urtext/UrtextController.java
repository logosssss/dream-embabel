package com.zhu.embabel.urtext;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回增强片段、原文和模型写的一句。
 */
@RestController
@RequestMapping({"/api/urtexts", "/api/urtexts/"})
public class UrtextController {

    private final UrtextService urtexts;

    public UrtextController(UrtextService urtexts) {
        this.urtexts = urtexts;
    }

    @PostMapping
    public ResponseEntity<UrtextResponse> write(@RequestBody UrtextRequest request) {
        UrtextResponse response = urtexts.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record UrtextRequest(String text) {
    }
}
