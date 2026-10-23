package com.zhu.embabel.agent;

import com.embabel.agent.api.validation.guardrails.UserInputGuardRail;
import com.embabel.agent.core.Blackboard;
import com.embabel.common.core.validation.ValidationError;
import com.embabel.common.core.validation.ValidationResult;
import com.embabel.common.core.validation.ValidationSeverity;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用户原文是空白时拒绝这次模型调用。有「# 用户输入」时只看这一节，否则看整段文字。
 */
@Component
public class TopicGuardRail implements UserInputGuardRail {

    public static final String NAME = "topicPresent";
    private static final String HEADING = "# 用户输入";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "用户输入不能是空白。";
    }

    @Override
    public ValidationResult validate(String content, Blackboard blackboard) {
        if (!topic(content).isBlank()) {
            return new ValidationResult(true, List.of());
        }
        return new ValidationResult(false, List.of(new ValidationError(
                "blank-topic",
                "用户输入是空白。",
                ValidationSeverity.ERROR)));
    }

    static String topic(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        int heading = content.indexOf(HEADING);
        if (heading < 0) {
            return content.trim();
        }
        String rest = content.substring(heading + HEADING.length());
        for (String line : rest.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                return "";
            }
            return trimmed;
        }
        return "";
    }
}
