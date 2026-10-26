package com.zhu.embabel.model;

import com.embabel.agent.api.common.ToolStats;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 演示页共用的启动检查和一次调用。各页仍返回自己的结果类型。
 */
public final class AgentRuns {

    private AgentRuns() {
    }

    public static String prepare(String text, ObjectProvider<LlmService<?>> models) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("内容不能是空白。");
        }
        requireModel(models);
        return text.trim();
    }

    public static void requireModel(ObjectProvider<LlmService<?>> models) {
        if (models.orderedStream().allMatch(model -> "local".equals(model.getProvider()))) {
            throw new ModelUnavailableException(LocalLlmService.MISSING);
        }
    }

    public static <T> AgentProcess run(AgentPlatform platform, String text, Class<T> resultType) {
        return AgentInvocation.create(platform, resultType).run(new UserInput(text));
    }

    /** 这次进程里实际调用过的工具名。没有调用则返回 null。 */
    public static String calledTools(AgentProcess process) {
        String tools = process.getToolsStats().getToolsStats().values().stream()
                .filter(stat -> stat.getCalls() > 0)
                .map(ToolStats::getName)
                .distinct()
                .collect(Collectors.joining("，"));
        return tools.isBlank() ? null : tools;
    }
}
