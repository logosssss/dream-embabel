package com.zhu.embabel.domain;

/**
 * 把主题交给子代理之后转述的一句。tool 是挂上的子代理工具名。
 */
public record SubagentLine(String tool, String text) {
}
