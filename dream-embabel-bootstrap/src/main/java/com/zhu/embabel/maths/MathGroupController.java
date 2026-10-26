package com.zhu.embabel.maths;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一道加法，拿回 math 工具组算出的结果。
 */
@RestController
@RequestMapping({"/api/math-sums", "/api/math-sums/"})
public class MathGroupController {

    private final MathGroupService sums;

    public MathGroupController(MathGroupService sums) {
        this.sums = sums;
    }

    @GetMapping("tools")
    public MathToolCatalog tools() {
        return sums.tools();
    }

    @PostMapping
    public ResponseEntity<MathGroupResponse> sum(@RequestBody MathGroupRequest request) {
        MathGroupResponse response = sums.sum(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record MathGroupRequest(String text) {
    }
}
