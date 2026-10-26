package com.zhu.embabel.branch;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回条件选中的分支。
 */
@RestController
@RequestMapping({"/api/branches", "/api/branches/"})
public class BranchController {

    private final BranchService branches;

    public BranchController(BranchService branches) {
        this.branches = branches;
    }

    @PostMapping
    public ResponseEntity<BranchResponse> branch(@RequestBody BranchRequest request) {
        BranchResponse response = branches.branch(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record BranchRequest(String text) {
    }
}
