package com.zhu.embabel.fallback;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.FallbackLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 按名字顺序跑一句。第一个没登记的名字会被跳过。
 */
@Service
public class FallbackService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public FallbackService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public FallbackResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, FallbackLine.class);
        FallbackLine line = process.last(FallbackLine.class);
        if (line == null) {
            return FallbackResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有兜底结果，进程状态：" + process.getStatus());
        }
        return FallbackResponse.completed(process.getId(), line);
    }
}
