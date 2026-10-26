# 从零学习 Embabel

[onboarding-plan.md](onboarding-plan.md) 解决的是把本仓库跑起来。这份文档解决的是理解框架：每个机制为什么存在，在 1.5.3 里落在哪个类上。

教材用《Embabel 框架深度学习手册（Java 视角）》，2026-10-22 那一版，锁 Embabel 1.5.3。手册第 22 章写了 2.0 预览，第一轮不要照着 main 分支改代码。查不到本仓库已经用过的签名时，以手册和 `docs/v0.1-verification.md` 为准，不要凭记忆补 API。

学的时候先读类型和测试，再打真实模型。打模型只用来核对「日志里的计划和耗时是否符合自己的预期」。

## 怎么过一课

每一课做三件事，做完再进下一课。

1. 读手册里点名的章，只读这一课列出的节。
2. 打开本仓库对应的类，先看方法签名和类型，再看方法体。
3. 不看手册，用自己的话回答这一课的问题。答不上就回到那个类，不要换一个更大的例子。

## 第一课：它在哪一层

读手册第 1 章、第 3 章。

Embabel 站在 Spring AI 上面。Spring AI 提供 ChatClient 这种一次补全。Embabel 决定下一次该执行哪个动作。只用一句问一句答时，框架多出来的是规划对象和进程，模型调用次数不会变少。

对照仓库根目录 README 里三个仓库的定位：StateGraph 是人把图画死，Embabel 是人声明每个动作要什么类型、产出什么类型。

问题：什么样的需求不该上这个框架？

## 第二课：一个动作就是一次调用

读手册第 5 章里 `@Agent`、`@Action`、`@AchievesGoal`，以及第 8 章里结构化对象。`@Export` 先知道它是对外目标名，这一课不用。

读 `QuickReplyAgent` 和 `QuickReplyAgentTest`。

这个 Agent 只有一个动作，输入是 `UserInput`，输出是 `QuickReply`。`creating(...).fromPrompt(...)` 在测试里记成一次 `CREATE_OBJECT`。没有工具，没有护栏，所以没有第二轮。

问题：如果把提示词改成「必须调用某个工具」，测试里的调用次数还会是 1 吗？

## 第三课：顺序从类型来，不从调用代码来

读手册第 6 章。

只看 `ContentReviewAgent` 四个公开动作的参数和返回值，先不读 prompt。再看 README 里那张「读取 / 产出」表。

`draftContent` 产出 `Draft`，`acceptDraft` 吃 `Draft` 产出 `AcceptedDraft`，后面的动作再吃这些类型。类里面没有 `draft(); accept(); review();` 这种顺序调用。`finalize` 上的 `@AchievesGoal` 告诉规划器：进程的目标是 `FinalArticle`。

问题：为什么加上 `QuickReplyAgent` 之后，`POST /api/reviews` 不会去走短回答？

## 第四课：规划器在本地搜索

读手册第 9 章，只读前置条件、效果和重规划。第 10 章的 Utility、Supervisor、Reflective 知道名字即可，本仓库没有切换规划器。

读 `PlanTraceListener`，再读 `reviewDraft` 里抛出 `ReplanRequestedException` 的那一段。

计划打在 logger `com.zhu.embabel.plan`。一次审稿的计划是四步：`draftContent`、`acceptDraft`、`reviewDraft`、`finalize`。分数低于 60 时，动作把初稿从黑板上藏起来并要求重规划，于是「写」和「审」会再出现。这不是模型自己决定下一步。

`finalize` 里面的 `RepeatUntilAcceptableBuilder` 是目标达成之后的内部循环，最多 3 轮。它不代替审校上的重规划。

问题：重规划多出来的耗时，是规划算法慢，还是同一类模型调用又跑了一遍？

## 第五课：模型调用长在动作里面

读手册第 7 章、第 8 章，以及第 13 章里按角色选模型的部分。

对照三处：

| 代码 | 模型怎么选 | 温度 |
|---|---|---|
| `QuickReplyAgent` | `LlmOptions.withDefaultLlm()` | 0.2，并限制 token |
| 初稿 `draftContent` | 角色 `writer` | 0.7 |
| 审校和定稿打分 | 角色 `reviewer` | 0.2 |

角色名要写进 `embabel.models.llms.writer` 和 `embabel.models.llms.reviewer`，值是已经登记的模型名。只配 `embabel.models.default-llm` 时，走角色的动作会找不到模型。

生成文本的运行时只接受 `SpringAiLlmService`。这一点在 `ChatClientLlmOperations` 里是类型检查，自写的 `LlmService` 不会被当成一次 Spring AI 调用。

问题：短回答为什么可以不配 writer / reviewer，审稿不行？

## 第六课：工具和检索是额外的一轮

读手册第 12 章的函数工具，以及第 15 章里 ToolishRag 和传统流水线 RAG 的差别。本仓库用的是进程内 Lucene，不是外部向量库。

读 `HouseStyleTools`、`StyleGuide`，再看 `ContentReviewAgent.writer` 如何 `withToolObject` 和 `withReference`。

`houseStyle` 本身不访问网络，但模型决定调用它时，框架要再请求一次模型把工具结果写回。提示词写了「调用 houseStyle」「用检索工具」，就会把这一轮变成高概率事件。规范已经用 `withReference` 放进上下文时，再强制查一遍，是在为同一次阅读付两次模型费用。

问题：审稿接口大约 90 秒，时间主要堆在哪一类步骤上？

## 第七课：人停在哪，进程停在哪

读手册第 17 章里和 HITL 有关的部分。MCP、Subagent、parallel 这一课不写代码。

读 `acceptDraft`。`dreamembabel.review.confirm=false` 时，这一步只是把 `Draft` 收成 `AcceptedDraft`，不调模型。打开之后，`WaitKt.confirm` 让进程进入 `WAITING`。HTTP 返回 202。确认由 shell 的 `x` 接收，不由页面这一次 POST 代填。

本仓库的 MCP 是可选客户端：不配置地址就不连。这个进程不是 MCP server。

问题：确认打开之后，90 秒会变成「等到人点确认」还是「模型调用变多」？

## 第八课：怎么证明自己没有看错

读手册第 18 章。

两条测试不要混：

- domain 里的 `FakeOperationContext`：不启动 Spring。用它核对 prompt、温度、调用次数、有没有工具。
- bootstrap 里继承 `EmbabelMockitoIntegrationTest`：整段 `AgentInvocation` 用 `whenCreateObject` 打桩。基类把默认模型叫做 `test-model`，测试必须注册同名服务，并且 provider 不能是 `local`。

真实 key 只留给自己手动打 `POST /api/replies` 和 `POST /api/reviews`。CI 不访问模型。

问题：哪条测试能证明「审稿链在假模型下会走完四次 creating」？

## 先不要学的部分

下面这些手册里有，但第一轮学了会和本仓库的写法打架。

| 手册内容 | 为什么先放下 |
|---|---|
| 第 10 章换规划器 | 本仓库只跑 GOAP。换规划器要先能解释现在的四步计划 |
| 第 14 章 ScatterGather、共识 | 本仓库没有多模型同时写同一篇稿 |
| 第 16 章 DICE、`@Memory` | 1.5.3 上这套记忆不稳定。审校记忆是本仓库的 `ReviewMemory`，进程内、最近一条 |
| 官方 `embabel-agent-starter-deepseek` | 手册写了这个 starter。本仓库走 OpenAI 兼容登记加 `deepseek` profile。两套不要同时改 |
| 第 19 章 Zipkin | 先能看懂 `com.zhu.embabel.plan` 的计划和执行日志 |
| 把 Agent 暴露成 MCP server | 本仓库只在配置了地址时当客户端 |

## 学完时能回答的五句话

1. 规划器选动作，模型只负责动作内部的生成或打分。
2. 动作顺序写在类型的输入和输出上，不写在一个总方法里。
3. 一次 HTTP 的等待约等于串行模型次数乘以单次延迟；工具和重规划都是在加次数。
4. 短回答是这个框架上的合法用法，它只有一个动作；慢的是审稿链，不是框架不能做短调用。
5. 无 key 能启动，是因为占位模型也是 `SpringAiLlmService`；真正的 profile 来自进程环境变量，不是来自 properties 里那一行 key。
