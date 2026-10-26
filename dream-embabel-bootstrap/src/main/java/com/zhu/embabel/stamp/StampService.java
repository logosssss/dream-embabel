package com.zhu.embabel.stamp;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.StampNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑盖章代理。工具由 fromInstance 在这次调用上挂上，不进工具组。
 */
@Service
public class StampService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public StampService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public StampResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, StampNote.class);
        StampNote note = process.last(StampNote.class);
        if (note == null) {
            return StampResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有盖章结果，进程状态：" + process.getStatus());
        }
        return StampResponse.completed(process.getId(), note, AgentRuns.calledTools(process));
    }

}
