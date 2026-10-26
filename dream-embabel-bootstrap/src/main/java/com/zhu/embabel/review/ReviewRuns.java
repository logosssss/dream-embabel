package com.zhu.embabel.review;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * 进行中的审稿。平台监听器按进程的 contextId 找到同一次进度。
 */
@Component
class ReviewRuns {

    private final ConcurrentMap<String, ReviewPlan> runs = new ConcurrentHashMap<>();

    ReviewPlan open() {
        ReviewPlan plan = new ReviewPlan(UUID.randomUUID().toString());
        runs.put(plan.runId(), plan);
        return plan;
    }

    ReviewPlan find(String runId) {
        if (runId == null || runId.isBlank()) {
            return null;
        }
        return runs.get(runId);
    }

    ReviewPlan require(String runId) {
        ReviewPlan plan = find(runId);
        if (plan == null) {
            throw new NoSuchElementException("没有这次审稿。");
        }
        return plan;
    }
}
