package com.zhu.embabel.model;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * 没有真实模型时占位。名字必须和 {@code embabel.models.default-llm} 一致。
 * 框架调用时要求 {@code SpringAiLlmService}，所以这里只提供一个会拒绝生成的 {@link ChatModel}。
 */
public final class LocalLlmService {

    public static final String MISSING = "没有配置对话模型。设置 OPENAI_API_KEY 或 DEEPSEEK_API_KEY 后重新启动。";

    private LocalLlmService() {
    }

    static ChatModel chatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new IllegalStateException(MISSING);
            }
        };
    }
}
