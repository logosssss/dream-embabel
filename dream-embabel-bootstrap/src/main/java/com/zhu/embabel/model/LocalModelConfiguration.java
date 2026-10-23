package com.zhu.embabel.model;

import com.embabel.agent.config.models.openai.OpenAiModelsConfig;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * 没有 key 时注册占位模型。openai / deepseek profile 改由 {@link OpenAiModelsImport} 登记真实模型。
 */
@Configuration
public class LocalModelConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LocalModelConfiguration.class);

    @Bean
    @Profile("!openai & !deepseek")
    SpringAiLlmService localLlm(@Value("${embabel.models.default-llm:local}") String name) {
        log.info("当前使用占位模型 {}。设置 OPENAI_API_KEY 或 DEEPSEEK_API_KEY 后重新启动，才会登记真实模型。", name);
        return new SpringAiLlmService(name, "local", LocalLlmService.chatModel());
    }

    @Configuration
    @Profile({"openai", "deepseek"})
    @Import(OpenAiModelsConfig.class)
    static class OpenAiModelsImport {
    }
}
