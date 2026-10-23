package com.zhu.embabel.knowledge;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

/**
 * 本地写作规范。模型通过 {@code @LlmTool} 查询，不访问网络。
 */
@Component
public class HouseStyleTools {

    public static final String NAME = "houseStyle";

    @LlmTool(name = NAME, description = "按栏目查询中文写作规范。栏目未知时返回专栏规范。")
    public String houseStyle(@LlmTool.Param(description = "栏目，例如专栏、评论、通讯") String section) {
        if (section == null || section.isBlank()) {
            return columnStyle();
        }
        return switch (section.trim()) {
            case "评论" -> "评论先给判断，再给依据。一段一个论点。";
            case "通讯" -> "通讯按时间写清人物、地点和结果。";
            default -> columnStyle();
        };
    }

    private static String columnStyle() {
        return "专栏用短段落。标题点题。不堆砌形容词。";
    }
}
