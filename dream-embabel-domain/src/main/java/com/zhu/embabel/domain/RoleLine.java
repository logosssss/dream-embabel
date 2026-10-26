package com.zhu.embabel.domain;

/**
 * 按角色写出的一句。role 是配置里的角色名，不是模型名。
 */
public record RoleLine(String role, String text) {
}
