package com.zhu.embabel.review;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次审稿的规划进度。动作名来自这次进程的计划事件，不写死步骤。
 */
final class ReviewPlan {

    private final String runId;
    private final List<Step> steps = new ArrayList<>();
    private String status = "RUNNING";
    private String processId;
    private String title;
    private String body;
    private String content;
    private String message;
    private boolean failed;

    ReviewPlan(String runId) {
        this.runId = runId;
    }

    String runId() {
        return runId;
    }

    synchronized void planned(List<String> names) {
        if (failed || names == null) {
            return;
        }
        List<Step> pool = new ArrayList<>(steps);
        List<Step> next = new ArrayList<>();
        for (String name : names) {
            Step kept = null;
            for (int i = 0; i < pool.size(); i++) {
                if (name.equals(pool.get(i).name)) {
                    kept = pool.remove(i);
                    break;
                }
            }
            next.add(kept == null ? new Step(name) : kept);
        }
        steps.clear();
        steps.addAll(next);
    }

    synchronized void started(String name) {
        if (failed || name == null) {
            return;
        }
        open(name).state = "running";
    }

    synchronized void finished(String name) {
        if (failed || name == null) {
            return;
        }
        Step step = findOpen(name);
        if (step != null) {
            step.state = "done";
        }
    }

    synchronized void failed(String name, String message) {
        if (failed) {
            Step step = find(name);
            if (step != null && step.message == null && message != null) {
                step.message = message;
                this.message = message;
            }
            return;
        }
        Step step = name == null ? running() : open(name);
        step.state = "failed";
        step.message = message;
        this.failed = true;
        this.status = "FAILED";
        this.message = message;
    }

    synchronized void stuck() {
        if (failed) {
            return;
        }
        Step step = running();
        failed(step == null ? "plan" : step.name, "规划无法继续");
    }

    synchronized void result(ContentReviewResponse response) {
        this.processId = response.processId();
        this.title = response.title();
        this.body = response.body();
        this.content = response.content();
        if (failed) {
            if (this.message == null) {
                this.message = response.message();
            }
            Step step = findFailed();
            if (step != null && step.message == null) {
                step.message = response.message();
            }
            return;
        }
        this.status = response.status();
        this.message = response.message();
        if ("COMPLETED".equals(response.status())) {
            for (Step step : steps) {
                if ("running".equals(step.state)) {
                    step.state = "done";
                }
            }
        }
    }

    synchronized boolean claimWaiting() {
        if (failed || !"WAITING".equals(status)) {
            return false;
        }
        status = "RUNNING";
        return true;
    }

    synchronized void crashed(String message) {
        if (!failed) {
            Step step = running();
            if (step != null) {
                failed(step.name, message);
                return;
            }
        }
        this.failed = true;
        this.status = "FAILED";
        if (this.message == null) {
            this.message = message;
        }
    }

    synchronized ReviewRunView view() {
        List<ReviewRunView.ReviewStep> copy = new ArrayList<>();
        for (Step step : steps) {
            copy.add(new ReviewRunView.ReviewStep(step.name, step.state, step.message));
        }
        return new ReviewRunView(runId, status, copy, processId, title, body, content, message);
    }

    private Step open(String name) {
        Step step = findOpen(name);
        if (step != null) {
            return step;
        }
        Step created = new Step(name);
        steps.add(created);
        return created;
    }

    private Step findOpen(String name) {
        for (Step step : steps) {
            if (name.equals(step.name) && !"done".equals(step.state) && !"failed".equals(step.state)) {
                return step;
            }
        }
        return null;
    }

    private Step find(String name) {
        for (Step step : steps) {
            if (name != null && name.equals(step.name)) {
                return step;
            }
        }
        return null;
    }

    private Step running() {
        for (Step step : steps) {
            if ("running".equals(step.state)) {
                return step;
            }
        }
        return null;
    }

    private Step findFailed() {
        for (Step step : steps) {
            if ("failed".equals(step.state)) {
                return step;
            }
        }
        return null;
    }

    private static final class Step {
        private final String name;
        private String state = "pending";
        private String message;

        private Step(String name) {
            this.name = name;
        }
    }
}
