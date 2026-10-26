package com.zhu.embabel.guide;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回按栏目规范写出的一句。
 */
@RestController
@RequestMapping({"/api/guide-lines", "/api/guide-lines/"})
public class GuideLineController {

    private final GuideLineService guides;

    public GuideLineController(GuideLineService guides) {
        this.guides = guides;
    }

    @GetMapping("tools")
    public GuideToolCatalog tools() {
        return guides.tools();
    }

    @PostMapping
    public ResponseEntity<GuideLineResponse> write(@RequestBody GuideLineRequest request) {
        GuideLineResponse response = guides.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record GuideLineRequest(String text) {
    }
}
