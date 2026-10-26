package com.zhu.embabel.stamp;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回先盖章再写的一句。
 */
@RestController
@RequestMapping({"/api/stamps", "/api/stamps/"})
public class StampController {

    private final StampService stamps;

    public StampController(StampService stamps) {
        this.stamps = stamps;
    }

    @PostMapping
    public ResponseEntity<StampResponse> write(@RequestBody StampRequest request) {
        StampResponse response = stamps.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record StampRequest(String text) {
    }
}
