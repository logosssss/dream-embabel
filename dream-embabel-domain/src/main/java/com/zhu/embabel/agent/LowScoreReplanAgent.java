package com.zhu.embabel.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.ReplanRequestedException;
import com.embabel.agent.domain.io.UserInput;
import com.zhu.embabel.domain.ReplanNote;
import com.zhu.embabel.domain.RetryMark;
import com.zhu.embabel.domain.TrialDraft;

/**
 * 两次都用默认模型，按用户主题写。第一稿故意只写半句，固定 40 分，低于 {@link #LINE} 就丢掉并重规划。
 * 第二稿把同一主题写成完整的一句，固定 90 分。分数不交给模型，是为了页面每次都能看见重规划。
 */
@Agent(description = "低分丢掉初稿后再写一稿")
public class LowScoreReplanAgent {

    /** 低于这个分数就重规划。 */
    public static final int LINE = 60;

    @Action(description = "第一次低分稿")
    public TrialDraft firstDraft(UserInput input, Ai ai) {
        String text = ai.withDefaultLlm().generateText(Prompts.quoted("""
                只根据用户主题写半句中文，故意不要写完。
                """, input.getContent()));
        return new TrialDraft(1, 40, 0, null, Texts.required(text));
    }

    @Action(description = "重规划后的及格稿")
    public TrialDraft rewrite(UserInput input, RetryMark mark, Ai ai) {
        String text = ai.withDefaultLlm().generateText("""
                用户主题如下。上一稿没有写完，这一稿写成完整的一句中文。
                不要解释，不要加引号，不要照抄主题原句，也不要重复上一稿。

                # 用户主题
                %s

                # 上一稿
                %s
                """.formatted(input.getContent(), mark.text()));
        return new TrialDraft(2, 90, mark.score(), mark.text(), Texts.required(text));
    }


    /**
     * 及格才达成目标。不及格时藏起当前稿并放上 {@link RetryMark}，
     * 规划器下一轮不能再选已经跑过的低分动作，只能选 {@link #rewrite}。
     */
    @AchievesGoal(description = "及格稿")
    @Action(description = "低于 60 分就重规划", canRerun = true)
    public ReplanNote accept(TrialDraft draft) {
        if (draft.score() < LINE) {
            throw new ReplanRequestedException(
                    "审校分数 " + draft.score() + " 低于 " + LINE + "，丢掉这一稿后重写",
                    blackboard -> {
                        blackboard.hide(draft);
                        blackboard.addObject(new RetryMark(draft.score(), draft.text()));
                    });
        }
        return new ReplanNote(
                draft.attempt(), draft.score(), draft.discardedScore(), draft.discardedText(), draft.text());
    }
}
