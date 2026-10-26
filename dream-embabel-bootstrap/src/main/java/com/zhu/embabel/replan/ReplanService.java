package com.zhu.embabel.replan;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.ReplanNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑一次低分重规划。两稿都调用默认模型，第一稿固定低于分数线。
 */
@Service
public class ReplanService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public ReplanService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public ReplanResponse replan(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, ReplanNote.class);
        ReplanNote note = process.last(ReplanNote.class);
        if (note == null) {
            return ReplanResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有重规划结果，进程状态：" + process.getStatus());
        }
        return ReplanResponse.completed(process.getId(), note);
    }
}
