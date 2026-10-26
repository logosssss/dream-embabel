package com.zhu.embabel.guide;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.GuideLine;
import com.zhu.embabel.guide.GuideToolCatalog.GuideToolLine;
import com.zhu.embabel.knowledge.StyleGuide;
import com.zhu.embabel.model.AgentRuns;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 把栏目规范挂成引用再跑一句。工具名单来自这个引用，不手写。
 */
@Service
public class GuideLineService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    private final StyleGuide guide;

    public GuideLineService(
            AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models, StyleGuide guide) {
        this.agentPlatform = agentPlatform;
        this.models = models;
        this.guide = guide;
    }

    public GuideToolCatalog tools() {
        List<GuideToolLine> lines = guide.reference().tools().stream()
                .map(Tool::getDefinition)
                .map(definition -> new GuideToolLine(definition.getName(), definition.getDescription()))
                .toList();
        return new GuideToolCatalog(StyleGuide.NAME, lines);
    }

    public GuideLineResponse write(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, GuideLine.class);
        GuideLine line = process.last(GuideLine.class);
        if (line == null) {
            return GuideLineResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有引用结果，进程状态：" + process.getStatus());
        }
        return GuideLineResponse.completed(process.getId(), line, AgentRuns.calledTools(process));
    }

}
