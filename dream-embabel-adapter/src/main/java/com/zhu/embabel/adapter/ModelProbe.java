package com.zhu.embabel.adapter;

import com.embabel.agent.api.common.Ai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 注入 {@link Ai}，启动时打一次 {@code generateText}。
 * 默认关闭。openai / deepseek profile 里才打开。没有 key 时不要打开。
 */
@Component
@ConditionalOnProperty(prefix = "dreamembabel.model-probe", name = "enabled", havingValue = "true")
public class ModelProbe implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ModelProbe.class);

    private final Ai ai;

    public ModelProbe(Ai ai) {
        this.ai = ai;
    }

    @Override
    public void run(ApplicationArguments args) {
        String text = ai.withDefaultLlm().generateText("Reply with the single word pong.");
        log.info("model probe result: {}", text);
    }
}
