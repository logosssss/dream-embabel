package com.zhu.embabel.adapter;

import com.embabel.agent.config.models.openai.OpenAiModelsConfig;
import com.embabel.agent.spi.LlmService;
import com.embabel.common.ai.model.PerTokenPricingModel;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 把 deepseek-flash 登记成一个 OpenAI 兼容模型。
 * 外层不引用 OpenAI 类，starter 不在运行时 classpath 上时内层不会加载。
 */
@Configuration
@Profile("deepseek")
@ConditionalOnClass(name = "com.embabel.agent.config.models.openai.OpenAiModelsConfig")
public class DeepSeekModelConfiguration {

    @Configuration
    @Profile("deepseek")
    static class Registration {

        @Bean
        LlmService<?> deepseekFlash(OpenAiModelsConfig models) {
            return models.openAiCompatibleLlm(
                    "deepseek-flash",
                    new PerTokenPricingModel(0.0, 0.0),
                    "Deepseek",
                    LocalDate.of(2026, 10, 17));
        }
    }
}
