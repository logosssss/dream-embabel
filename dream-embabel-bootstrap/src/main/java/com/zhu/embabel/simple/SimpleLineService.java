package com.zhu.embabel.simple;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.SimpleLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 把主题交给 SimpleAgentBuilder 子流程，让模型写一句。
 */
@Service
public class SimpleLineService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public SimpleLineService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public SimpleLineResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, SimpleLine.class);
        SimpleLine line = process.last(SimpleLine.class);
        if (line == null) {
            return SimpleLineResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有简式结果，进程状态：" + process.getStatus());
        }
        return SimpleLineResponse.completed(process.getId(), line);
    }
}
