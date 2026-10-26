package com.zhu.embabel.reply;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 一句话进，一句短回答出。
 */
@RestController
@RequestMapping({"/api/replies", "/api/replies/"})
public class QuickReplyController {

    private final QuickReplyService replies;

    public QuickReplyController(QuickReplyService replies) {
        this.replies = replies;
    }

    @PostMapping
    public ResponseEntity<QuickReplyResponse> reply(@RequestBody QuickReplyRequest request) {
        QuickReplyResponse response = replies.reply(request.message());
        return RunStatus.respond(response.status(), response);
    }

    public record QuickReplyRequest(String message) {
    }
}
