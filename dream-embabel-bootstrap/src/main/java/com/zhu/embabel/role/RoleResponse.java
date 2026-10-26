package com.zhu.embabel.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhu.embabel.domain.RoleLine;

/**
 * 按角色调用的 HTTP 结果。空字段不写出。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleResponse(String processId, String status, String role, String text, String message) {

    static RoleResponse completed(String processId, RoleLine line) {
        return new RoleResponse(processId, "COMPLETED", line.role(), line.text(), null);
    }

    static RoleResponse unfinished(String processId, String status, String message) {
        return new RoleResponse(processId, status, null, null, message);
    }

}
