package com.zhu.embabel.echo;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回 echo 工具名和模型写的一句。
 */
@RestController
@RequestMapping({"/api/echoes", "/api/echoes/"})
public class EchoController {

    private final EchoService echoes;

    public EchoController(EchoService echoes) {
        this.echoes = echoes;
    }

    @PostMapping
    public ResponseEntity<EchoResponse> write(@RequestBody EchoRequest request) {
        EchoResponse response = echoes.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record EchoRequest(String text) {
    }
}
