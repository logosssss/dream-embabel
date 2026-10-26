package com.zhu.embabel.branch;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.BranchNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 按主题跑一次条件分支。被放行的那一支会调用模型。
 */
@Service
public class BranchService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public BranchService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public BranchResponse branch(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, BranchNote.class);
        BranchNote note = process.last(BranchNote.class);
        if (note == null) {
            return BranchResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有分支结果，进程状态：" + process.getStatus());
        }
        return BranchResponse.completed(process.getId(), note);
    }
}
