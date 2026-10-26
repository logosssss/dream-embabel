package com.zhu.embabel.subagent;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回子代理工具名和转述的一句。
 */
@RestController
@RequestMapping({"/api/subagents", "/api/subagents/"})
public class SubagentController {

    private final SubagentService subagents;

    public SubagentController(SubagentService subagents) {
        this.subagents = subagents;
    }

    @PostMapping
    public ResponseEntity<SubagentResponse> write(@RequestBody SubagentRequest request) {
        SubagentResponse response = subagents.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record SubagentRequest(String text) {
    }
}
