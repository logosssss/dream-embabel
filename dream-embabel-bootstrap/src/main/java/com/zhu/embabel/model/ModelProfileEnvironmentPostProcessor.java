package com.zhu.embabel.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 在配置文件加载前，按进程环境变量打开 Spring profile。
 * 登记在 {@code META-INF/spring.factories}。Boot 4.1 不读取 {@code META-INF/spring/} 下的同名文件。
 * IDE 直接跑 main 不会激活 Maven profile，所以不能靠构建期过滤 {@code spring.profiles.active}。
 */
public class ModelProfileEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String AUTO_PROFILE = "dreamembabel.models.auto-profile";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if ("false".equalsIgnoreCase(System.getProperty(AUTO_PROFILE))) {
            return;
        }
        if (present(environment, "DEEPSEEK_API_KEY")) {
            environment.addActiveProfile("deepseek");
            // Embabel 只看 OPENAI_API_KEY 是否为 null。空字符串也会挡住 properties 里的 DeepSeek 密钥。
            if (!present(environment, "OPENAI_API_KEY")) {
                Map<String, Object> bridge = new LinkedHashMap<>();
                bridge.put("OPENAI_API_KEY", environment.getProperty("DEEPSEEK_API_KEY"));
                environment.getPropertySources().addFirst(new MapPropertySource("dreamembabelDeepseekKey", bridge));
            }
            return;
        }
        if (present(environment, "OPENAI_API_KEY")) {
            environment.addActiveProfile("openai");
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 1;
    }

    private static boolean present(ConfigurableEnvironment environment, String name) {
        String value = environment.getProperty(name);
        return value != null && !value.isBlank();
    }
}
