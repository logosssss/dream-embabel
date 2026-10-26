package com.zhu.embabel.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.Method;
import com.embabel.common.ai.model.ByRoleModelSelectionCriteria;
import com.zhu.embabel.domain.RoleLine;
import org.junit.jupiter.api.Test;

class RoleAgentTest {

    private final RoleAgent agent = new RoleAgent();

    @Test
    void reviewTopicUsesTheReviewerRoleAndOtherTopicsUseWriter() {
        FakeOperationContext context = FakeOperationContext.create();
        context.expectResponse("Java 是一门跨平台的编程语言。");
        context.expectResponse("这句话还没说清跨平台指什么。");

        RoleLine written = agent.write(new UserInput("用一句话介绍 Java"), context.ai());
        RoleLine reviewed = agent.write(new UserInput("请审这段介绍"), context.ai());

        assertEquals(RoleAgent.WRITER, written.role());
        assertEquals("Java 是一门跨平台的编程语言。", written.text());
        assertEquals(RoleAgent.REVIEWER, reviewed.role());
        assertEquals("这句话还没说清跨平台指什么。", reviewed.text());
        assertRole(context, 0, RoleAgent.WRITER, "用一句话介绍 Java", "直接用一句中文介绍");
        assertRole(context, 1, RoleAgent.REVIEWER, "请审这段介绍", "一个最该改的问题");
    }

    private static void assertRole(
            FakeOperationContext context, int index, String role, String topic, String job) {
        var call = context.getLlmInvocations().get(index);
        assertEquals(Method.CREATE_OBJECT, call.getMethod());
        ByRoleModelSelectionCriteria criteria =
                assertInstanceOf(ByRoleModelSelectionCriteria.class, call.getInteraction().getLlm().getCriteria());
        assertEquals(role, criteria.getRole());
        assertTrue(call.getPrompt().contains(topic));
        assertTrue(call.getPrompt().contains(job));
    }
}
