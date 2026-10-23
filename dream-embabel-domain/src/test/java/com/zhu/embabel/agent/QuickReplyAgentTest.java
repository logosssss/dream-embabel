package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.LlmInvocation;
import com.embabel.agent.test.unit.Method;
import com.zhu.embabel.domain.QuickReply;
import org.junit.jupiter.api.Test;

class QuickReplyAgentTest {

    private final QuickReplyAgent agent = new QuickReplyAgent();

    @Test
    void replyMakesOneShortCallWithoutTools() {
        FakeOperationContext context = FakeOperationContext.create();
        QuickReply expected = new QuickReply("春天来了。");
        context.expectResponse(expected);

        QuickReply actual = agent.reply(new UserInput("用一句话介绍春天"), context.ai());

        assertEquals(expected, actual);
        assertEquals(1, context.getLlmInvocations().size());
        LlmInvocation call = context.getLlmInvocations().get(0);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        assertTrue(call.getPrompt().contains("用一两句中文直接回答"));
        assertTrue(call.getPrompt().contains("用一句话介绍春天"));
        assertTrue(call.getPrompt().contains("不要调用工具"));
        assertTrue(call.getInteraction().getTools().isEmpty());
        assertTrue(call.getInteraction().getGuardRails().isEmpty());
        assertEquals(0.2, call.getInteraction().getLlm().getTemperature());
        assertEquals(QuickReplyAgent.MAX_TOKENS, call.getInteraction().getLlm().getMaxTokens());
    }
}
