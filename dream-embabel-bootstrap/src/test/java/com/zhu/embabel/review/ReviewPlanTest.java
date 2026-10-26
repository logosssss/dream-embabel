package com.zhu.embabel.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewPlanTest {

    @Test
    void lightsStepsInPlanOrder() {
        ReviewPlan plan = new ReviewPlan("run-1");
        plan.planned(List.of("draftContent", "reviewDraft", "finalize"));
        plan.started("draftContent");

        ReviewRunView view = plan.view();
        assertEquals("running", view.steps().get(0).state());
        assertEquals("pending", view.steps().get(1).state());
        assertEquals("draftContent", view.steps().get(0).name());

        plan.finished("draftContent");
        plan.started("reviewDraft");
        assertEquals("done", plan.view().steps().get(0).state());
        assertEquals("running", plan.view().steps().get(1).state());
    }

    @Test
    void failureStopsTheChainAndKeepsTheError() {
        ReviewPlan plan = new ReviewPlan("run-2");
        plan.planned(List.of("draftContent", "reviewDraft"));
        plan.started("draftContent");
        plan.failed("draftContent", "This request requires apiKey");
        plan.started("reviewDraft");
        plan.finished("draftContent");
        plan.result(ContentReviewResponse.unfinished("p1", "FAILED", "没有定稿，进程状态：FAILED"));

        ReviewRunView view = plan.view();
        assertEquals("FAILED", view.status());
        assertEquals("failed", view.steps().get(0).state());
        assertEquals("This request requires apiKey", view.steps().get(0).message());
        assertEquals("pending", view.steps().get(1).state());
        assertEquals("p1", view.processId());
        assertNull(view.title());
    }

    @Test
    void laterFailureMessageFillsABlankStep() {
        ReviewPlan plan = new ReviewPlan("run-3");
        plan.planned(List.of("draftContent"));
        plan.failed("draftContent", null);
        plan.failed("draftContent", "缺少密钥");

        assertEquals("缺少密钥", plan.view().steps().get(0).message());
        assertEquals("FAILED", plan.view().status());
    }

    @Test
    void claimWaitingRunsAgainOnlyOnce() {
        ReviewPlan plan = new ReviewPlan("run-4");
        plan.result(ContentReviewResponse.waiting("p9", "确认采用这篇初稿并进入审校？", "河边", "春草发芽"));
        assertEquals("河边", plan.view().title());
        assertEquals("春草发芽", plan.view().body());
        assertEquals(true, plan.claimWaiting());
        assertEquals("RUNNING", plan.view().status());
        assertEquals(false, plan.claimWaiting());
    }
}
