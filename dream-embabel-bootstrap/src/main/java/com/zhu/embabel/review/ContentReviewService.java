package com.zhu.embabel.review;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.hitl.ConfirmationRequest;
import com.embabel.agent.core.hitl.ConfirmationResponse;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.Draft;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.model.AgentRuns;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 内容审校的唯一入口。shell 的 {@code review} 命令和 {@code POST /api/reviews} 都走这里，
 * 避免两条通道各写一套规划调用。
 *
 * <p>调用按目标类型 {@link FinalArticle} 发起，不点名 Agent。规划器在本地根据动作的
 * 输入、输出类型搜索路径，顺利时是初稿、确认、审校、定稿。{@code run} 会堵住当前线程，
 * 直到进程完成、停在人工确认，或无法再推进。一次成稿因此会等完串行的多次模型调用。
 *
 * <p>控制器把返回值映射成 HTTP：完成 200，等待确认 202，其余未完成 409。
 * 空白主题和没有真实模型在这里直接抛异常，由控制器收成 400 和 503。
 */
@Service
public class ContentReviewService {

    /**
     * 进程停在初稿确认时的说明。页面可以提交确认，shell 的 {@code x} 仍然可用。
     */
    static final String WAITING_MESSAGE = "初稿在等确认。确认后才会进入审校。";

    /** 已部署的 Agent 和规划器都在这个平台上。 */
    private final AgentPlatform agentPlatform;

    /**
     * 当前进程里登记的全部对话模型。用 {@link ObjectProvider} 是为了在调用前查看，
     * 而不是在构造服务时强制拿到某一个。
     */
    private final ObjectProvider<LlmService<?>> models;

    /** 页面轮询时，审稿在这条执行器上跑，请求线程立刻返回。 */
    private final Executor reviewRuns;

    /** 按运行号保存进度。平台监听器用同一个表接收规划事件。 */
    private final ReviewRuns runs;

    @Autowired
    public ContentReviewService(
            AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models, ReviewRuns runs) {
        this(agentPlatform, models, runnable -> {
            Thread thread = new Thread(runnable, "review-run");
            thread.setDaemon(true);
            thread.start();
        }, runs);
    }

    ContentReviewService(
            AgentPlatform agentPlatform,
            ObjectProvider<LlmService<?>> models,
            Executor reviewRuns,
            ReviewRuns runs) {
        this.agentPlatform = agentPlatform;
        this.models = models;
        this.reviewRuns = reviewRuns;
        this.runs = runs;
    }

    /**
     * 先返回运行号。规划动作在 {@link ReviewPlan#view()} 里更新，页面用它在等待时画链路。
     */
    public String start(String topic) {
        validate(topic);
        ReviewPlan plan = runs.open();
        String topicText = topic.trim();
        reviewRuns.execute(() -> {
            try {
                plan.result(review(topicText, plan.runId()));
            } catch (RuntimeException ex) {
                plan.crashed(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
            }
        });
        return plan.runId();
    }

    public ReviewRunView progress(String runId) {
        return runs.require(runId).view();
    }

    /**
     * 采用当前初稿并继续跑。对应 shell 里对确认请求回答接受。
     */
    public void confirm(String runId) {
        ReviewPlan plan = runs.require(runId);
        String processId = plan.view().processId();
        if (!plan.claimWaiting()) {
            throw new IllegalArgumentException("这次审稿不在等待确认。");
        }
        reviewRuns.execute(() -> continueAfterConfirm(plan, processId));
    }

    private void continueAfterConfirm(ReviewPlan plan, String processId) {
        try {
            AgentProcess process = agentPlatform.getAgentProcess(processId);
            if (process == null) {
                plan.crashed("找不到这个进程。");
                return;
            }
            ConfirmationRequest<?> request = process.last(ConfirmationRequest.class);
            if (request == null) {
                plan.crashed("没有待确认的初稿。");
                return;
            }
            request.onResponse(new ConfirmationResponse(
                    UUID.randomUUID().toString(), request.getId(), true, false, Instant.now()), process);
            plan.result(outcome(process.run()));
        } catch (RuntimeException ex) {
            plan.crashed(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    /**
     * 按主题跑完一次审校。
     *
     * @param topic 文章主题，会原样进 {@link UserInput}，再写入初稿提示词。不是口令或模板名
     * @return 定稿、等待确认，或带着进程状态的未完成结果
     * @throws IllegalArgumentException 主题为 null 或空白
     * @throws IllegalStateException 每个已登记模型的 provider 都是 {@code local}，消息为 {@link LocalLlmService#MISSING}
     */
    public ContentReviewResponse review(String topic) {
        return review(topic, null);
    }

    private ContentReviewResponse review(String topic, String runId) {
        validate(topic);
        AgentInvocation<FinalArticle> invocation = AgentInvocation.create(agentPlatform, FinalArticle.class);
        if (runId != null) {
            // 规划事件只到平台监听器。运行号放进 contextId，监听器才能对上这次进度。
            invocation = invocation.withProcessOptions(ProcessOptions.DEFAULT.withContextId(runId));
        }
        // 目标是 FinalArticle，起点是用户原文。短回答 Agent 的目标类型不同，不会被这条调用选中。
        AgentProcess process = invocation.run(new UserInput(topic.trim()));
        return outcome(process);
    }

    private ContentReviewResponse outcome(AgentProcess process) {
        // 只在 dreamembabel.review.confirm=true 时出现。默认确认关闭，这一步不会停。
        if (process.getStatus() == AgentProcessStatusCode.WAITING) {
            Draft draft = process.last(Draft.class);
            ConfirmationRequest<?> pending = process.last(ConfirmationRequest.class);
            String message = pending == null || pending.getMessage() == null
                    ? WAITING_MESSAGE
                    : pending.getMessage();
            return ContentReviewResponse.waiting(
                    process.getId(),
                    message,
                    draft == null ? null : draft.title(),
                    draft == null ? null : draft.body());
        }
        // 黑板上还有初稿和审校。取该类型的最后一条，才是定稿目标。
        FinalArticle article = process.last(FinalArticle.class);
        if (article == null) {
            // 失败、规划中断或目标类型没写上黑板。状态名原样返回，方便对照规划日志。
            return ContentReviewResponse.unfinished(
                    process.getId(),
                    process.getStatus().name(),
                    "没有定稿，进程状态：" + process.getStatus());
        }
        return ContentReviewResponse.completed(process.getId(), article);
    }

    private void validate(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("主题不能是空白。");
        }
        // 占位模型也能通过启动，但 ChatClient 生成时会拒绝它。这里提前失败，
        // 避免规划已经跑起来才在线程池里抛类型错误。集成测试的模型 provider 是 test，不会被拦下。
        // 没有任何模型时 allMatch 同样为真。
        AgentRuns.requireModel(models);
    }
}
