package com.zhu.embabel.utility;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回 Utility 规划器选中的一支。
 */
@RestController
@RequestMapping({"/api/utilities", "/api/utilities/"})
public class UtilityController {

    private final UtilityService utilities;

    public UtilityController(UtilityService utilities) {
        this.utilities = utilities;
    }

    @PostMapping
    public ResponseEntity<UtilityResponse> choose(@RequestBody UtilityRequest request) {
        UtilityResponse response = utilities.choose(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record UtilityRequest(String text) {
    }
}
