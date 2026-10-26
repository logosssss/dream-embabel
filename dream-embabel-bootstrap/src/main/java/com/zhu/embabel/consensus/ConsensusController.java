package com.zhu.embabel.consensus;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回两路判断和代码比较出的结论。
 */
@RestController
@RequestMapping({"/api/consensus", "/api/consensus/"})
public class ConsensusController {

    private final ConsensusService consensus;

    public ConsensusController(ConsensusService consensus) {
        this.consensus = consensus;
    }

    @PostMapping
    public ResponseEntity<ConsensusResponse> write(@RequestBody ConsensusRequest request) {
        ConsensusResponse response = consensus.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record ConsensusRequest(String text) {
    }
}
