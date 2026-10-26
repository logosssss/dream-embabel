package com.zhu.embabel.role;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.RoleLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 按角色跑一句。角色名来自配置，不在代码里写模型名。
 */
@Service
public class RoleService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public RoleService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public RoleResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, RoleLine.class);
        RoleLine line = process.last(RoleLine.class);
        if (line == null) {
            return RoleResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有角色结果，进程状态：" + process.getStatus());
        }
        return RoleResponse.completed(process.getId(), line);
    }
}
