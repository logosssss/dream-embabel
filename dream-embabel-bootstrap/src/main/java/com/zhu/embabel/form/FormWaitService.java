package com.zhu.embabel.form;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.hitl.FormBindingRequest;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.embabel.ux.form.Control;
import com.embabel.ux.form.Form;
import com.embabel.ux.form.TextField;
import com.zhu.embabel.domain.ColumnBrief;
import com.zhu.embabel.domain.FormLine;
import com.zhu.embabel.form.FormWaitResponse.FormField;
import com.zhu.embabel.model.AgentRuns;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 先把进程停在表单。提交后把填好的栏目要求放回黑板，再让模型写一句。
 */
@Service
public class FormWaitService {

    private final AgentPlatform agentPlatform;

    private final ObjectProvider<LlmService<?>> models;

    public FormWaitService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
    }

    public FormWaitResponse start() {
        requireModel();
        AgentProcess process = AgentInvocation.create(agentPlatform, FormLine.class)
                .run(new UserInput("请填写栏目要求"));
        if (process.getStatus() != AgentProcessStatusCode.WAITING) {
            return FormWaitResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "进程没有停在表单，状态：" + process.getStatus());
        }
        FormBindingRequest<?> request = formOf(process);
        Form form = request.getPayload();
        return FormWaitResponse.waiting(process.getId(), form.getTitle(), fields(form));
    }

    public FormWaitResponse submit(String processId, String column, String point) {
        if (processId == null || processId.isBlank()) {
            throw new IllegalArgumentException("没有进程。");
        }
        if (column == null || column.isBlank() || point == null || point.isBlank()) {
            throw new IllegalArgumentException("栏目和要点都不能是空白。");
        }
        requireModel();
        AgentProcess process = agentPlatform.getAgentProcess(processId);
        if (process == null) {
            throw new IllegalArgumentException("找不到这个进程。");
        }
        FormBindingRequest<ColumnBrief> request = formOf(process);
        request.bind(new ColumnBrief(column.trim(), point.trim()), process);
        process.run();
        FormLine line = process.last(FormLine.class);
        if (line == null) {
            return FormWaitResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "表单提交后没有写出句子，进程状态：" + process.getStatus());
        }
        return FormWaitResponse.completed(process.getId(), line.text());
    }

    private void requireModel() {
        AgentRuns.requireModel(models);
    }

    @SuppressWarnings("unchecked")
    private static FormBindingRequest<ColumnBrief> formOf(AgentProcess process) {
        Object last = process.lastResult();
        if (last instanceof FormBindingRequest<?> request && request.getOutputClass() == ColumnBrief.class) {
            return (FormBindingRequest<ColumnBrief>) request;
        }
        throw new IllegalStateException("进程没有停在栏目表单上。");
    }

    private static List<FormField> fields(Form form) {
        List<FormField> fields = new ArrayList<>();
        for (Control control : form.getControls()) {
            String label = control instanceof TextField text ? text.getLabel() : control.getId();
            fields.add(new FormField(control.getId(), label));
        }
        return fields;
    }
}
