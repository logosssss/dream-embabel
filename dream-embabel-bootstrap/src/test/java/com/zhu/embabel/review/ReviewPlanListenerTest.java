package com.zhu.embabel.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.embabel.agent.api.event.ActionExecutionResultEvent;
import com.embabel.agent.api.event.ActionExecutionStartEvent;
import com.embabel.agent.api.event.AgentProcessPlanFormulatedEvent;
import com.embabel.agent.api.event.LlmCallFailedEvent;
import com.embabel.agent.api.event.LlmRequestEvent;
import com.embabel.agent.core.ActionStatus;
import com.embabel.agent.core.ActionStatusCode;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.plan.Plan;
import com.embabel.plan.WorldState;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewPlanListenerTest {

    @Test
    void readsActionNamesFromPlanEvents() {
        ReviewRuns runs = new ReviewRuns();
        ReviewPlan plan = runs.open();
        ReviewPlanListener listener = new ReviewPlanListener(runs);
        AgentProcess process = processFor(plan.runId());
        com.embabel.agent.core.Action draft = action("draftContent");
        com.embabel.agent.core.Action review = action("reviewDraft");
        Plan formulated = mock(Plan.class);
        when(formulated.getActions()).thenReturn(List.of(draft, review));

        listener.onProcessEvent(new AgentProcessPlanFormulatedEvent(process, mock(WorldState.class), formulated));
        listener.onProcessEvent(new ActionExecutionStartEvent(process, draft));
        listener.onProcessEvent(new ActionExecutionResultEvent(
                process, draft, new ActionStatus(Duration.ZERO, ActionStatusCode.SUCCEEDED), Duration.ZERO));
        listener.onProcessEvent(new ActionExecutionStartEvent(process, review));
        listener.onProcessEvent(failure(process, review, "This request requires apiKey"));

        ReviewRunView view = plan.view();
        assertEquals("done", view.steps().get(0).state());
        assertEquals("failed", view.steps().get(1).state());
        assertEquals("reviewDraft", view.steps().get(1).name());
        assertEquals("This request requires apiKey", view.steps().get(1).message());
        assertEquals("FAILED", view.status());
    }

    @Test
    void ignoresEventsFromAnotherProcess() {
        ReviewRuns runs = new ReviewRuns();
        ReviewPlan plan = runs.open();
        ReviewPlanListener listener = new ReviewPlanListener(runs);
        AgentProcess process = processFor("other-run");
        com.embabel.agent.core.Action draft = action("draftContent");
        Plan formulated = mock(Plan.class);
        when(formulated.getActions()).thenReturn(List.of(draft));

        listener.onProcessEvent(new AgentProcessPlanFormulatedEvent(process, mock(WorldState.class), formulated));

        assertEquals(0, plan.view().steps().size());
    }

    private static AgentProcess processFor(String runId) {
        AgentProcess process = mock(AgentProcess.class);
        ProcessOptions options = mock(ProcessOptions.class);
        when(options.getContextIdString()).thenReturn(runId);
        when(process.getProcessOptions()).thenReturn(options);
        return process;
    }

    private static com.embabel.agent.core.Action action(String name) {
        com.embabel.agent.core.Action action = mock(com.embabel.agent.core.Action.class);
        when(action.getName()).thenReturn(name);
        return action;
    }

    @SuppressWarnings("unchecked")
    private static LlmCallFailedEvent failure(AgentProcess process, com.embabel.agent.core.Action action, String message) {
        LlmRequestEvent<String> request = mock(LlmRequestEvent.class);
        when(request.getAction()).thenReturn(action);
        when(request.getAgentProcess()).thenReturn(process);
        return new LlmCallFailedEvent(request, new IllegalStateException(message), 1, 5, Duration.ZERO);
    }
}
