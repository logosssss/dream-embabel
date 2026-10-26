package com.zhu.embabel.tool;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * 折在目录工具里面的两个方法。模型要先展开目录，才会看到它们。
 */
public class DeskTools {

    public static final String COUNT = "countChars";

    public static final String MARK = "firstMark";

    @LlmTool(name = COUNT, description = "数出主题有几个字，返回「字数：N」。")
    public String countChars(String topic) {
        String trimmed = topic == null ? "" : topic.trim();
        return "字数：" + trimmed.codePointCount(0, trimmed.length());
    }

    @LlmTool(name = MARK, description = "取主题的第一个字，返回「首字：X」。")
    public String firstMark(String topic) {
        String trimmed = topic == null ? "" : topic.trim();
        if (trimmed.isEmpty()) {
            return "首字：无";
        }
        int end = trimmed.offsetByCodePoints(0, 1);
        return "首字：" + trimmed.substring(0, end);
    }
}
