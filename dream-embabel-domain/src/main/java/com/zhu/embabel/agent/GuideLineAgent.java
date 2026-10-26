package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.GuideLine;
import com.zhu.embabel.knowledge.StyleGuide;

/**
 * 把栏目规范作为引用挂到这次调用上。模型自己检索，再按查到的内容写一句。
 */
@Agent(description = "按栏目规范写一句")
public class GuideLineAgent {

    private final StyleGuide guide;

    public GuideLineAgent(StyleGuide guide) {
        this.guide = guide;
    }

    @AchievesGoal(description = "按规范写一句")
    @Action(description = "先检索栏目规范，再写一句")
    public GuideLine write(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().withReference(guide.reference()).generateText("""
                用户主题如下。必须先用栏目规范的检索工具查资料，再只用一句中文回应。
                句子要用到查到的内容，不要解释，不要照抄主题原句。

                # 用户主题
                %s
                """.formatted(input.getContent()));
        return new GuideLine(StyleGuide.NAME, Texts.required(text));
    }

}
