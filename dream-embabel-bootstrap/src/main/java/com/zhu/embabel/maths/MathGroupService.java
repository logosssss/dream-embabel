package com.zhu.embabel.maths;

import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.spi.LlmService;
import com.embabel.agent.tools.math.MathTools;
import com.zhu.embabel.domain.MathAnswer;
import com.zhu.embabel.model.AgentRuns;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 跑数学工具组代理。工具组要先在平台里注册，调用时按名字 math 挂上。
 */
@Service
public class MathGroupService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    private final MathTools mathTools;

    public MathGroupService(
            AgentPlatform agentPlatform,
            ObjectProvider<LlmService<?>> models,
            MathTools mathTools) {
        this.agentPlatform = agentPlatform;
        this.models = models;
        this.mathTools = mathTools;
    }

    public MathToolCatalog tools() {
        return catalog(mathTools);
    }

    static MathToolCatalog catalog(MathTools tools) {
        String visible = tools.getTools().stream()
                .map(tool -> tool.getDefinition().getName())
                .collect(Collectors.joining("，"));
        List<MathToolCatalog.MathToolLine> lines = tools.getInnerTools().stream()
                .map(MathGroupService::line)
                .toList();
        return new MathToolCatalog(CoreToolGroups.MATH, visible, lines);
    }

    public MathGroupResponse sum(String text) {
        text = AgentRuns.prepare(text, models);
        AgentProcess process = AgentRuns.run(agentPlatform, text, MathAnswer.class);
        MathAnswer answer = process.last(MathAnswer.class);
        if (answer == null) {
            return MathGroupResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有计算结果，进程状态：" + process.getStatus());
        }
        return MathGroupResponse.completed(process.getId(), answer, AgentRuns.calledTools(process));
    }

    private static MathToolCatalog.MathToolLine line(Tool tool) {
        return new MathToolCatalog.MathToolLine(
                tool.getDefinition().getName(),
                tool.getDefinition().getDescription());
    }

}
