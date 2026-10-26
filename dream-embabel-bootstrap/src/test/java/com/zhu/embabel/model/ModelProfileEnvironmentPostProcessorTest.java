package com.zhu.embabel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class ModelProfileEnvironmentPostProcessorTest {

    @Test
    void blankOpenaiKeyDoesNotHideDeepseekKey() {
        StandardEnvironment environment = environmentWith(
                "OPENAI_API_KEY", "",
                "DEEPSEEK_API_KEY", "deepseek-test-key");

        new ModelProfileEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertEquals("deepseek-test-key", environment.getProperty("OPENAI_API_KEY"));
        assertEquals("deepseek", environment.getActiveProfiles()[0]);
    }

    @Test
    void realOpenaiKeyStaysWhenBothKeysExist() {
        StandardEnvironment environment = environmentWith(
                "OPENAI_API_KEY", "openai-test-key",
                "DEEPSEEK_API_KEY", "deepseek-test-key");

        new ModelProfileEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertEquals("openai-test-key", environment.getProperty("OPENAI_API_KEY"));
        assertFalse(environment.getPropertySources().contains("dreamembabelDeepseekKey"));
    }

    private static StandardEnvironment environmentWith(String... pairs) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            values.put(pairs[i], pairs[i + 1]);
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        return environment;
    }
}
