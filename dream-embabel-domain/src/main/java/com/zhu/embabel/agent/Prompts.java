package com.zhu.embabel.agent;

/**
 * 演示页写一句时共用的收尾。各页只提供自己的要求。
 */
public final class Prompts {

    private static final String PLAIN = "不要解释，不要照抄主题原句。";

    private static final String NO_QUOTES = "不要解释，不要加引号，不要照抄主题原句。";

    private Prompts() {
    }

    public static String topic(String instruction, String topic) {
        return finish(instruction, PLAIN, topic);
    }

    public static String quoted(String instruction, String topic) {
        return finish(instruction, NO_QUOTES, topic);
    }

    /** 主题后面还要附原文、记忆或上一轮。 */
    public static String topicThen(String instruction, String topic, String rest) {
        return finish(instruction, PLAIN, topic) + "\n" + rest.strip() + "\n";
    }

    public static String quotedThen(String instruction, String topic, String rest) {
        return finish(instruction, NO_QUOTES, topic) + "\n" + rest.strip() + "\n";
    }

    private static String finish(String instruction, String rule, String topic) {
        return """
                %s
                %s

                # 用户主题
                %s
                """.formatted(instruction.strip(), rule, topic);
    }
}
