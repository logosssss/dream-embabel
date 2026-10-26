package com.zhu.embabel.export;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回导出目标名和模型写的一句。
 */
@RestController
@RequestMapping({"/api/exports", "/api/exports/"})
public class ExportController {

    private final ExportService exports;

    public ExportController(ExportService exports) {
        this.exports = exports;
    }

    @PostMapping
    public ResponseEntity<ExportResponse> write(@RequestBody ExportRequest request) {
        ExportResponse response = exports.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record ExportRequest(String text) {
    }
}
