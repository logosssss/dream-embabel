package com.zhu.embabel.review;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.LlmService;
import com.zhu.embabel.domain.FinalArticle;
import com.zhu.embabel.model.LocalLlmService;
import org.springframework.beans.factory.ObjectProvider;
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
     * 进程停在初稿确认时给调用方的说明。确认只能由 shell 的 {@code x} 接收，
     * 这次调用不代填确认。
     */
    static final String WAITING_MESSAGE = "初稿在等确认。请用 x，由 shell 接收确认后再继续。";

    /** 已部署的 Agent 和规划器都在这个平台上。 */
    private final AgentPlatform agentPlatform;

    /**
     * 当前进程里登记的全部对话模型。用 {@link ObjectProvider} 是为了在调用前查看，
     * 而不是在构造服务时强制拿到某一个。
     */
    private final ObjectProvider<LlmService<?>> models;

    public ContentReviewService(AgentPlatform agentPlatform, ObjectProvider<LlmService<?>> models) {
        this.agentPlatform = agentPlatform;
        this.models = models;
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
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("主题不能是空白。");
        }
        // 占位模型也能通过启动，但 ChatClient 生成时会拒绝它。这里提前失败，
        // 避免规划已经跑起来才在线程池里抛类型错误。集成测试的模型 provider 是 test，不会被拦下。
        // 没有任何模型时 allMatch 同样为真。
        if (models.orderedStream().allMatch(model -> "local".equals(model.getProvider()))) {
            throw new IllegalStateException(LocalLlmService.MISSING);
        }
        // 目标是 FinalArticle，起点是用户原文。短回答 Agent 的目标类型不同，不会被这条调用选中。
        AgentProcess process = AgentInvocation.create(agentPlatform, FinalArticle.class)
                .run(new UserInput(topic.trim()));
        // 只在 dreamembabel.review.confirm=true 时出现。默认确认关闭，这一步不会停。
        if (process.getStatus() == AgentProcessStatusCode.WAITING) {
            return ContentReviewResponse.waiting(process.getId(), WAITING_MESSAGE);
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
}
