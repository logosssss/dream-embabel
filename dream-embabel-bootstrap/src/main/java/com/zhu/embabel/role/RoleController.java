package com.zhu.embabel.role;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一段主题，拿回按角色写出的一句。
 */
@RestController
@RequestMapping({"/api/roles", "/api/roles/"})
public class RoleController {

    private final RoleService roles;

    public RoleController(RoleService roles) {
        this.roles = roles;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> write(@RequestBody RoleRequest request) {
        RoleResponse response = roles.write(request.text());
        return RunStatus.respond(response.status(), response);
    }

    public record RoleRequest(String text) {
    }
}
