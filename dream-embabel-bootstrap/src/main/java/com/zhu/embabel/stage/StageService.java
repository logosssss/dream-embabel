package com.zhu.embabel.stage;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.SupervisorNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 走已经标成 Supervisor 的阶段代理。监督者和准备稿都会调用默认模型。
 */
@Service
public class StageService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public StageService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public StageResponse run(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, SupervisorNote.class);
        SupervisorNote note = process.last(SupervisorNote.class);
        if (note == null) {
            return StageResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有监督结果，进程状态：" + process.getStatus());
        }
        return StageResponse.completed(process.getId(), note);
    }
}
