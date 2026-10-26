package com.zhu.embabel.unfold;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.UnfoldNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑渐进披露代理。一开始只挂目录工具，展开后才出现里面的方法。
 */
@Service
public class UnfoldService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public UnfoldService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public UnfoldResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, UnfoldNote.class);
        UnfoldNote note = process.last(UnfoldNote.class);
        if (note == null) {
            return UnfoldResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有展开结果，进程状态：" + process.getStatus());
        }
        return UnfoldResponse.completed(process.getId(), note, AgentRuns.calledTools(process));
    }

}
