package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.FallbackByNameModelSelectionCriteria;
import com.zhu.embabel.domain.FallbackLine;
import java.util.List;
import org.junit.jupiter.api.Test;

class FallbackAgentTest {

    @Test
    void asksForTheMissingNameBeforeDeepSeek() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 是一门跨平台的编程语言。");

        FallbackLine line = new FallbackAgent().write(new UserInput("用一句话介绍 Java"), context.ai());

        assertEquals(FallbackAgent.FIRST, line.first());
        assertEquals(FallbackAgent.SECOND, line.second());
        assertEquals("Java 是一门跨平台的编程语言。", line.text());
        var call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        FallbackByNameModelSelectionCriteria criteria = assertInstanceOf(
                FallbackByNameModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertEquals(List.of(FallbackAgent.FIRST, FallbackAgent.SECOND), criteria.getNames());
        assertTrue(call.getPrompt().contains("用一句话介绍 Java"));
        assertTrue(call.getPrompt().contains("不要照抄主题原句"));
    }
}
