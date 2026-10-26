package com.zhu.embabel.auto;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.AutoNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 用 {@code withAutoLlm} 跑一次说明。短回答走默认模型，这里不走那一档。
 */
@Service
public class AutoReplyService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public AutoReplyService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public AutoReplyResponse reply(String message) {
        message = AgentRuns.prepare(message, models);
        AgentProcess process = AgentRuns.run(agentPlatform, message, AutoNote.class);
        AutoNote note = process.last(AutoNote.class);
        if (note == null) {
            return AutoReplyResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有说明，进程状态：" + process.getStatus());
        }
        return AutoReplyResponse.completed(process.getId(), note);
    }
}
