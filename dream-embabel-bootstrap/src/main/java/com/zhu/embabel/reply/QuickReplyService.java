package com.zhu.embabel.reply;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.QuickReply;
import com.zhu.embabel.model.LocalLlmService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 一次调用只生成短回答，不进入审稿链路。
 */
@Service
public class QuickReplyService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public QuickReplyService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public QuickReplyResponse reply(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("内容不能是空白。");
        }
        if (models.orderedStream().allMatch(model -> "local".equals(model.getProvider()))) {
            throw new IllegalStateException(LocalLlmService.MISSING);
        }
        AgentProcess process = AgentInvocation.create(agentPlatform, QuickReply.class)
                .run(new UserInput(message.trim()));
        QuickReply reply = process.last(QuickReply.class);
        if (reply == null) {
            return QuickReplyResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有回答，进程状态：" + process.getStatus());
        }
        return QuickReplyResponse.completed(process.getId(), reply);
    }
}
