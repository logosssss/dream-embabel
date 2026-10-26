package com.zhu.embabel.memory;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.MemoryLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 按标签取出一条记忆，再让模型写一句。
 */
@Service
public class MemoryService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public MemoryService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public MemoryResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, MemoryLine.class);
        MemoryLine line = process.last(MemoryLine.class);
        if (line == null) {
            return MemoryResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有记忆结果，进程状态：" + process.getStatus());
        }
        return MemoryResponse.completed(process.getId(), line);
    }
}
