package com.zhu.embabel.reflect;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.ReflectiveNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑反思代理。循环内部两轮都会调用默认模型。
 */
@Service
public class ReflectiveService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public ReflectiveService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public ReflectiveResponse run(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, ReflectiveNote.class);
        ReflectiveNote note = process.last(ReflectiveNote.class);
        if (note == null) {
            return ReflectiveResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有反思结果，进程状态：" + process.getStatus());
        }
        return ReflectiveResponse.completed(process.getId(), note);
    }
}
