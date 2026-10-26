package com.zhu.embabel.consensus;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.ConsensusNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 两路判断同一个主题。比较由代码完成。
 */
@Service
public class ConsensusService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public ConsensusService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public ConsensusResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, ConsensusNote.class);
        ConsensusNote note = process.last(ConsensusNote.class);
        if (note == null) {
            return ConsensusResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有共识结果，进程状态：" + process.getStatus());
        }
        return ConsensusResponse.completed(process.getId(), note);
    }
}
