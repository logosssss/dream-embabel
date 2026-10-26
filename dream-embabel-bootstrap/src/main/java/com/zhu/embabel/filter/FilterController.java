package com.zhu.embabel.filter;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回过滤后的资料和模型写的一句。
 */
@RestController
@RequestMapping({"/api/filters", "/api/filters/"})
public class FilterController {

    private final FilterService filters;

    public FilterController(FilterService filters) {
        this.filters = filters;
    }

    @PostMapping
    public ResponseEntity<FilterResponse> write(@RequestBody FilterRequest request) {
        FilterResponse response = filters.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record FilterRequest(String text) {
    }
}
