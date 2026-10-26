package com.zhu.embabel.named;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.NamedLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 跑带调用 id 的代理，并把当前日志人格一并返回。
 */
@Service
public class NamedLineService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    private final String personality;

    public NamedLineService(
            AgentPlatform agentPlatform,
            ObjectProvider<LlmService<?>> models,
            @Value("${embabel.agent.logging.personality:}") String personality) {
        this.agentPlatform = agentPlatform;
        this.models = models;
        this.personality = personality;
    }

    public NamedLineResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, NamedLine.class);
        NamedLine line = process.last(NamedLine.class);
        if (line == null) {
            return NamedLineResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有命名结果，进程状态：" + process.getStatus());
        }
        String shown = personality == null || personality.isBlank() ? null : personality.trim();
        return NamedLineResponse.completed(process.getId(), line, shown);
    }
}
