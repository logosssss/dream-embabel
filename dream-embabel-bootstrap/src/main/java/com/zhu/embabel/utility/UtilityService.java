package com.zhu.embabel.utility;

import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.UtilityChoice;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 用 Utility 规划器选一支，再让选中的动作调用默认模型。
 */
@Service
public class UtilityService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public UtilityService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public UtilityResponse choose(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentInvocation.create(agentPlatform, UtilityChoice.class)
                .withProcessOptions(ProcessOptions.DEFAULT.withPlannerType(PlannerType.UTILITY))
                .run(new UserInput(text.trim()));
        UtilityChoice choice = process.last(UtilityChoice.class);
        if (choice == null) {
            return UtilityResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有效用结果，进程状态：" + process.getStatus());
        }
        return UtilityResponse.completed(process.getId(), choice);
    }
}
