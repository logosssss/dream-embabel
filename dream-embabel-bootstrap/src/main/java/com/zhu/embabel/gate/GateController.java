package com.zhu.embabel.gate;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回按剧本写好的一句。
 */
@RestController
@RequestMapping({"/api/gates", "/api/gates/"})
public class GateController {

    private final GateService gates;

    public GateController(GateService gates) {
        this.gates = gates;
    }

    @PostMapping
    public ResponseEntity<GateResponse> write(@RequestBody GateRequest request) {
        GateResponse response = gates.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record GateRequest(String text) {
    }
}
