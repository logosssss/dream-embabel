package com.zhu.embabel.knowledge;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示页查看规范库，并做一次全文检索。
 */
@RestController
@RequestMapping({"/api/style-guide", "/api/style-guide/"})
public class StyleGuideController {

    private final StyleGuideDemo guide;

    public StyleGuideController(StyleGuideDemo guide) {
        this.guide = guide;
    }

    @GetMapping
    public StyleGuideDemo.StyleGuideStatus status() {
        return guide.status();
    }

    @PostMapping({"/searches", "/searches/"})
    public StyleGuideSearch search(@RequestBody StyleGuideQuery query) {
        return new StyleGuideSearch(guide.search(query.query()));
    }

    public record StyleGuideQuery(String query) {
    }

    public record StyleGuideSearch(String text) {
    }
}
