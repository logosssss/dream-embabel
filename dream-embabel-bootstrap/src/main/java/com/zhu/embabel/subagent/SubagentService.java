package com.zhu.embabel.subagent;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.SubagentLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑宿主代理。简式代理以 Subagent 工具的形式挂在这次调用上。
 */
@Service
public class SubagentService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public SubagentService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public SubagentResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, SubagentLine.class);
        SubagentLine line = process.last(SubagentLine.class);
        if (line == null) {
            return SubagentResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有子代理结果，进程状态：" + process.getStatus());
        }
        return SubagentResponse.completed(process.getId(), line, AgentRuns.calledTools(process));
    }

}
