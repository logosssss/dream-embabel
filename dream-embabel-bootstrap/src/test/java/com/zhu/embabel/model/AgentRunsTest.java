package com.zhu.embabel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.embabel.agent.spi.LlmService;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class AgentRunsTest {

    @Test
    void blankTextIsRejectedBeforeTheModelCheck() {
        assertThrows(IllegalArgumentException.class, () -> AgentRuns.prepare("  ", models("deepseek")));
    }

    @Test
    void localPlaceholderIsUnavailable() {
        assertThrows(ModelUnavailableException.class, () -> AgentRuns.prepare("用一句话介绍 Java", models("local")));
    }

    @Test
    void namedModelReturnsTheTrimmedTopic() {
        assertEquals("用一句话介绍 Java", AgentRuns.prepare(" 用一句话介绍 Java ", models("deepseek")));
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<LlmService<?>> models(String provider) {
        LlmService<?> model = mock(LlmService.class);
        when(model.getProvider()).thenReturn(provider);
        ObjectProvider<LlmService<?>> models = mock(ObjectProvider.class);
        when(models.orderedStream()).thenAnswer(invocation -> Stream.of(model));
        return models;
    }
}
