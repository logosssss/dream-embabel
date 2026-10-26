package com.zhu.embabel.prefetch;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.PrefetchLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 开口前预取栏目规范，再让模型写一句。
 */
@Service
public class PrefetchService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public PrefetchService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public PrefetchResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, PrefetchLine.class);
        PrefetchLine line = process.last(PrefetchLine.class);
        if (line == null) {
            return PrefetchResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有预取结果，进程状态：" + process.getStatus());
        }
        return PrefetchResponse.completed(process.getId(), line);
    }
}
