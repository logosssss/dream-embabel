package com.zhu.embabel.scatter;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.ScatterNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 把主题分两路跑完再合成。两路都会调用模型。
 */
@Service
public class ScatterService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public ScatterService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public ScatterResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, ScatterNote.class);
        ScatterNote note = process.last(ScatterNote.class);
        if (note == null) {
            return ScatterResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有分发结果，进程状态：" + process.getStatus());
        }
        return ScatterResponse.completed(process.getId(), note);
    }
}
