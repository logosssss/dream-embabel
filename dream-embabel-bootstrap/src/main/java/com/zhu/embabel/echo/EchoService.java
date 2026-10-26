package com.zhu.embabel.echo;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.EchoLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑 echo 代理。工具是本机 MCP 客户端，不在这里另起进程。
 */
@Service
public class EchoService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public EchoService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public EchoResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, EchoLine.class);
        EchoLine line = process.last(EchoLine.class);
        if (line == null) {
            return EchoResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有 echo 结果，进程状态：" + process.getStatus());
        }
        return EchoResponse.completed(process.getId(), line, AgentRuns.calledTools(process));
    }

}
