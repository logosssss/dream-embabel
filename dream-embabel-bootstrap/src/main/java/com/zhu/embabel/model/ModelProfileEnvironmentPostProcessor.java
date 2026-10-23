package com.zhu.embabel.model;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

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
        if (present("DEEPSEEK_API_KEY")) {
            environment.addActiveProfile("deepseek");
            return;
        }
        if (present("OPENAI_API_KEY")) {
            environment.addActiveProfile("openai");
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 1;
    }

    private static boolean present(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }
}
