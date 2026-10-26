package com.zhu.embabel.review;

import com.embabel.agent.api.event.AbstractAgentProcessEvent;
import com.embabel.agent.api.event.ActionExecutionResultEvent;
import com.embabel.agent.api.event.ActionExecutionStartEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessPlanFormulatedEvent;
import com.embabel.agent.api.event.AgentProcessStuckEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.api.event.LlmCallFailedEvent;
import com.embabel.agent.core.ActionStatusCode;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.plan.Action;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 规划、开始、结束都发给平台上的 {@link AgenticEventListener}。
 * 进程选项上的监听器收不到这些事件。页面这次审稿把运行号写进 contextId，这里按它更新进度。
 */
@Component
class ReviewPlanListener implements AgenticEventListener {

    private final ReviewRuns runs;

    ReviewPlanListener(ReviewRuns runs) {
        this.runs = runs;
    }

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        if (!(event instanceof AbstractAgentProcessEvent processEvent)) {
            return;
        }
        AgentProcess process = processEvent.getAgentProcess();
        if (process == null) {
            return;
        }
        ProcessOptions options = process.getProcessOptions();
        if (options == null) {
            return;
        }
        ReviewPlan plan = runs.find(options.getContextIdString());
        if (plan == null) {
            return;
        }
        if (event instanceof AgentProcessPlanFormulatedEvent planned) {
            List<String> names = new ArrayList<>();
            for (Action action : planned.getPlan().getActions()) {
                names.add(action.getName());
            }
            plan.planned(names);
        } else if (event instanceof ActionExecutionStartEvent started) {
            plan.started(started.getAction().getName());
        } else if (event instanceof ActionExecutionResultEvent result) {
            String name = result.getAction().getName();
            ActionStatusCode status = result.getActionStatus().getStatus();
            if (status == ActionStatusCode.FAILED) {
                plan.failed(name, null);
            } else if (status == ActionStatusCode.SUCCEEDED || status == ActionStatusCode.WAITING) {
                plan.finished(name);
            }
        } else if (event instanceof LlmCallFailedEvent failed) {
            String name = failed.getRequest().getAction().getName();
            Throwable cause = failed.getThrowable();
            plan.failed(name, cause == null ? null : cause.getMessage());
        } else if (event instanceof AgentProcessStuckEvent) {
            plan.stuck();
        }
    }
}
