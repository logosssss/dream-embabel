package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.Extracted;
import com.zhu.embabel.domain.MaybeNote;

/**
 * 尽量抽出一个对象。{@code createObjectIfPossible} 抽不到时返回空，动作改记成没抽到，
 * 不把用户原话当成结果。
 */
@Agent(description = "尽量从一句话里抽出标题和正文")
public class MaybeNoteAgent {

    @AchievesGoal(description = "抽出标题和正文，或明确没抽到")
    @Action(description = "createObjectIfPossible 一次调用")
    public MaybeNote extract(UserInput input, Ai ai) {
        Extracted extracted = ai.withDefaultLlm().createObjectIfPossible("""
                从用户输入抽出一个短标题和一句正文。
                抽不到就不要编造，这次可以失败。

                # 用户输入
                %s
                """.formatted(input.getContent()), Extracted.class);
        if (extracted == null || extracted.title() == null || extracted.title().isBlank()) {
            return new MaybeNote(false, null, null);
        }
        return new MaybeNote(true, extracted.title(), extracted.body());
    }
}
