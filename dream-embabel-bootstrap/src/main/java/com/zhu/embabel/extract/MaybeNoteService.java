package com.zhu.embabel.extract;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.MaybeNote;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 用 createObjectIfPossible 抽一次。抽不到也是完成，只是 found 为 false。
 */
@Service
public class MaybeNoteService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public MaybeNoteService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public MaybeNoteResponse extract(String message) {
        message = AgentRuns.prepare(message, models);
        AgentProcess process = AgentRuns.run(agentPlatform, message, MaybeNote.class);
        MaybeNote note = process.last(MaybeNote.class);
        if (note == null) {
            return MaybeNoteResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有抽取结果，进程状态：" + process.getStatus());
        }
        return MaybeNoteResponse.completed(process.getId(), note);
    }
}
