package com.zhu.embabel.export;

import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Export;
import com.embabel.agent.core.Goal;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.agent.ExportLineAgent;
import com.zhu.embabel.domain.ExportLine;
import com.zhu.embabel.model.AgentRuns;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑导出目标，并把元数据里的导出名和 remote 一并返回。
 */
@Service
public class ExportService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public ExportService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public ExportResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        Export export = published();
        AgentProcess process = AgentRuns.run(agentPlatform, text, ExportLine.class);
        ExportLine line = process.last(ExportLine.class);
        if (line == null) {
            return ExportResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有导出结果，进程状态：" + process.getStatus());
        }
        return ExportResponse.completed(process.getId(), export.getName(), export.getRemote(), line.text());
    }

    private static Export published() {
        return new AgentMetadataReader()
                .createAgentMetadata(new ExportLineAgent())
                .getGoals()
                .stream()
                .map(Goal::getExport)
                .filter(export -> ExportLineAgent.EXPORTED.equals(export.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("没有找到导出目标。"));
    }
}
