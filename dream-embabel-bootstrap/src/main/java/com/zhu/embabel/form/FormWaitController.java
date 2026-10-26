package com.zhu.embabel.form;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 先停在表单，提交后再拿模型写的一句。
 */
@RestController
@RequestMapping({"/api/forms", "/api/forms/"})
public class FormWaitController {

    private final FormWaitService forms;

    public FormWaitController(FormWaitService forms) {
        this.forms = forms;
    }

    @PostMapping
    public ResponseEntity<FormWaitResponse> start() {
        FormWaitResponse response = forms.start();
        return RunStatus.respond(response.status(), response);
    }

    @PostMapping("submit")
    public ResponseEntity<FormWaitResponse> submit(@RequestBody FormSubmit request) {
        FormWaitResponse response = forms.submit(request.processId(), request.column(), request.point());
        return RunStatus.respond(response.status(), response);
    }

    public record FormSubmit(String processId, String column, String point) {
    }
}
