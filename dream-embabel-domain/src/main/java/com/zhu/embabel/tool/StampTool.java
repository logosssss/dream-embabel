package com.zhu.embabel.tool;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * 一个普通 Java 对象。{@code Tool.fromInstance} 只收带 {@link LlmTool} 的方法。
 */
public class StampTool {

    public static final String NAME = "stamp";

    @LlmTool(name = NAME, description = "数出主题有几个字，返回「章：N字」。必须把返回值原样写在回答第一行。")
    public String stamp(String topic) {
        String trimmed = topic == null ? "" : topic.trim();
        int count = trimmed.codePointCount(0, trimmed.length());
        return "章：" + count + "字";
    }
}
