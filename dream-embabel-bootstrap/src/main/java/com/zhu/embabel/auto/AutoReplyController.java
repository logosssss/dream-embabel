package com.zhu.embabel.auto;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一句话，用自动选中的模型拿回一句说明。
 */
@RestController
@RequestMapping({"/api/auto-replies", "/api/auto-replies/"})
public class AutoReplyController {

    private final AutoReplyService replies;

    public AutoReplyController(AutoReplyService replies) {
        this.replies = replies;
    }

    @PostMapping
    public ResponseEntity<AutoReplyResponse> reply(@RequestBody AutoReplyRequest request) {
        AutoReplyResponse response = replies.reply(request.message());
        return RunStatus.respond(response.status(), response);
    }

    public record AutoReplyRequest(String message) {
    }
}
