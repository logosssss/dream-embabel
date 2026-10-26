package com.zhu.embabel.text;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.TextLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 用 generateText 跑一次纯文本。不走 creating。
 */
@Service
public class TextLineService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public TextLineService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public TextLineResponse line(String message) {
        message = AgentRuns.prepare(message, models);
        AgentProcess process = AgentRuns.run(agentPlatform, message, TextLine.class);
        TextLine line = process.last(TextLine.class);
        if (line == null) {
            return TextLineResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有文本，进程状态：" + process.getStatus());
        }
        return TextLineResponse.completed(process.getId(), line);
    }
}
