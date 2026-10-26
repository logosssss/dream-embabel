package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.TextLine;

/**
 * 只要一段文字。{@code generateText} 返回字符串，再装进 {@link TextLine}。
 * 短回答和自动页用的是 {@code creating}，不走这里。
 */
@Agent(description = "用默认模型生成一段纯文本")
public class TextLineAgent {

    @AchievesGoal(description = "写出一段纯文本")
    @Action(description = "generateText 一次调用")
    public TextLine line(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().generateText("""
                用一句中文说出这段话的要点。
                这是纯文本，不要输出字段名，不要照抄原句。

                # 用户输入
                %s
                """.formatted(input.getContent()));
        return new TextLine(text);
    }
}
