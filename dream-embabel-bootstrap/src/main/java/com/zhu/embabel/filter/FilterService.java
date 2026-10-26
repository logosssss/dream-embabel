package com.zhu.embabel.filter;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.FilterLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 按章节过滤栏目规范，再让模型写一句。
 */
@Service
public class FilterService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public FilterService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public FilterResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, FilterLine.class);
        FilterLine line = process.last(FilterLine.class);
        if (line == null) {
            return FilterResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有过滤结果，进程状态：" + process.getStatus());
        }
        return FilterResponse.completed(process.getId(), line);
    }
}
