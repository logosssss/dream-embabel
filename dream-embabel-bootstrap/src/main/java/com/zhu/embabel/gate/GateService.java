package com.zhu.embabel.gate;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.GateNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑剧本代理。取首字的工具在数字数之前会被框架拒绝。
 */
@Service
public class GateService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public GateService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public GateResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, GateNote.class);
        GateNote note = process.last(GateNote.class);
        if (note == null) {
            return GateResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有剧本结果，进程状态：" + process.getStatus());
        }
        return GateResponse.completed(process.getId(), note, AgentRuns.calledTools(process));
    }

}
