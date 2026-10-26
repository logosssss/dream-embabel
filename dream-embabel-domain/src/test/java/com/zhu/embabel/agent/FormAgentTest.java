package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.core.hitl.AwaitableResponseException;
import com.embabel.agent.core.hitl.FormBindingRequest;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.ux.form.Control;
import com.embabel.ux.form.Form;
import com.zhu.embabel.domain.ColumnBrief;
import com.zhu.embabel.domain.FormLine;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FormAgentTest {

    @Test
    void askStopsOnAFormForTheColumnBrief() {
        FormAgent agent = new FormAgent();

        AwaitableResponseException error = assertThrows(
                AwaitableResponseException.class, () -> agent.ask(new UserInput("补一条栏目要求")));

        FormBindingRequest<?> request = assertInstanceOf(FormBindingRequest.class, error.getAwaitable());
        assertEquals(ColumnBrief.class, request.getOutputClass());
        Form form = assertInstanceOf(Form.class, request.getPayload());
        assertEquals(FormAgent.TITLE, form.getTitle());
        String ids = form.getControls().stream().map(Control::getId).collect(Collectors.joining(","));
        assertTrue(ids.contains("column"), ids);
        assertTrue(ids.contains("point"), ids);
    }

    @Test
    void writeUsesTheSubmittedBrief() {
        FormAgent agent = new FormAgent();
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("专栏要在标题里点出春草发芽。");

        FormLine line = agent.write(new ColumnBrief("专栏", "春草发芽，标题要点题"), context.ai());

        assertEquals("专栏要在标题里点出春草发芽。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertInstanceOf(DefaultModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertTrue(call.getPrompt().contains("专栏"));
        assertTrue(call.getPrompt().contains("春草发芽，标题要点题"));
        assertTrue(call.getPrompt().contains("不要照抄原句"));
    }
}
