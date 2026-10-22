package com.zhu.embabel;

import com.embabel.agent.api.event.ActionExecutionStartEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessPlanFormulatedEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.core.IoBinding;
import com.embabel.plan.Plan;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用框架公开的进程事件打印计划顺序和实际执行顺序。
 * logger 名 com.zhu.embabel.plan，单独开关。
 */
@Component
public class PlanTraceListener implements AgenticEventListener {

    static final String LOGGER_NAME = "com.zhu.embabel.plan";

    private static final Logger log = LoggerFactory.getLogger(LOGGER_NAME);

    private final ConcurrentHashMap<String, AtomicInteger> runSeq = new ConcurrentHashMap<>();

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        if (event instanceof AgentProcessPlanFormulatedEvent planned) {
            logPlan(planned);
        } else if (event instanceof ActionExecutionStartEvent started) {
            logRun(started);
        }
    }

    private void logPlan(AgentProcessPlanFormulatedEvent event) {
        String processId = event.getProcessId();
        runSeq.put(processId, new AtomicInteger());
        Plan plan = event.getPlan();
        int index = 1;
        for (com.embabel.plan.Action action : plan.getActions()) {
            String inputs = "-";
            String outputs = "-";
            if (action instanceof com.embabel.agent.core.Action step) {
                inputs = types(step.getInputs());
                outputs = types(step.getOutputs());
            }
            log.info("plan process={} seq={} action={} in={} out={}",
                    processId, index, action.getName(), inputs, outputs);
            index++;
        }
    }

    private void logRun(ActionExecutionStartEvent event) {
        int seq = runSeq.computeIfAbsent(event.getProcessId(), id -> new AtomicInteger()).incrementAndGet();
        log.info("run process={} seq={} action={}",
                event.getProcessId(), seq, event.getAction().getName());
    }

    private static String types(Set<IoBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return "-";
        }
        return bindings.stream()
                .map(PlanTraceListener::simpleType)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private static String simpleType(IoBinding binding) {
        String raw = binding.getValue();
        int colon = raw.indexOf(':');
        String type = colon >= 0 ? raw.substring(colon + 1) : raw;
        int dot = type.lastIndexOf('.');
        return dot >= 0 ? type.substring(dot + 1) : type;
    }
}
