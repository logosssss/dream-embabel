package com.zhu.embabel.urtext;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.UrtextLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 把栏目规范原文交给模型，写一句。
 */
@Service
public class UrtextService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public UrtextService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public UrtextResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, UrtextLine.class);
        UrtextLine line = process.last(UrtextLine.class);
        if (line == null) {
            return UrtextResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有原文结果，进程状态：" + process.getStatus());
        }
        return UrtextResponse.completed(process.getId(), line);
    }
}
