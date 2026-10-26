# dream-embabel

基于 [Embabel](https://github.com/embabel/embabel-agent) 的 Java Agent 工程化基座。领域类型决定动作能读什么、能产出什么，规划器按前置条件和效果做确定性搜索。

同定位的另外两个仓库：

- [dream-ai](https://github.com/logosssss/dream-ai)：Spring Boot 上的模块化单体。对话走工具循环，图编排用 Spring AI Alibaba 的 StateGraph。
- [dream-scope](https://github.com/logosssss/dream-scope)：基于 AgentScope Java 2.0 的模块化单体。同步和流式入口都走 HarnessAgent。
- dream-embabel（本仓库）：用 GOAP 做目标驱动的确定性规划。审稿是完整链路，其余页面各演示一项能力。

## 版本

| 项 | 版本 |
|---|---|
| JDK | 21。`JAVA_HOME` 必须是 21，Wrapper 不会替你切换 |
| Maven | 仓库自带 Wrapper 3.9.11。Windows 用 `mvnw.cmd`，其他系统用 `./mvnw` |
| Spring Boot | 4.1.1 |
| Embabel | 1.5.3 |
| Spring AI | 2.0.1，由 Embabel 传递。Milvus store 单独对齐到 2.0.1 |
| 页面 | Vue 3 + Vue Router + Vite 6。`dream-embabel-ui` 不在 Maven 里 |

核实记录见 [docs/v0.1-verification.md](docs/v0.1-verification.md)。

## 快速开始

仓库里没有密钥。复制 [`.env.example`](.env.example) 为工作目录或上一级目录的 `.env` 再填写。启动时读取这个文件，进程里已经存在的环境变量优先。`.env` 不要提交。

### 只跑测试

测试不调用真实模型，也不连接 MySQL 或 Milvus。

```text
mvnw -B test
```

`skipTests` 已是 `false`。CI 在 Temurin 21 上执行 `./mvnw -B verify -DskipTests=false`，不构建页面。

### 启动

当前 [`application.properties`](dream-embabel-bootstrap/src/main/resources/application.properties) 会做这些事：

- 进程快照写入本机 MySQL 8，库名 `dream_embabel`。先执行 [`agent-process-snapshots-mysql.sql`](dream-embabel-bootstrap/src/main/resources/db/agent-process-snapshots-mysql.sql)。主机、账号来自 `MYSQL_HOST`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`，主机默认 `127.0.0.1`。
- 向量检索连接本机 Milvus `127.0.0.1:19530`，库 `default`，集合 `style_guide`。账号来自 `MILVUS_USERNAME`、`MILVUS_PASSWORD`，留空则不带认证。全文检索和相邻片段仍走进程内 Lucene。向量是字符哈希，不调用外部 embedding 接口。
- `dreamembabel.mcp.echo.enabled=true`，用 stdio 拉起本机 echo。
- `dreamembabel.model-probe.enabled=true`，启动时打一次模型。没有 key 时占位模型会拒绝这次调用。
- `dreamembabel.review.confirm=true`，审稿初稿会停住等人确认。

有 `DEEPSEEK_API_KEY` 时打开 profile `deepseek`，base 为 `https://api.deepseek.com`，模型为 `deepseek-flash`。只有 `OPENAI_API_KEY` 时打开 profile `openai`，默认模型是 `gpt-4.1-mini`。两把 key 都在时用 DeepSeek。不需要再开 Maven profile。

```text
mvnw -pl dream-embabel-bootstrap -am spring-boot:run
```

Tomcat 监听 8080。`embabel.agent.shell.web-application-type=servlet` 负责把 Embabel shell 的 Web 类型从 `none` 改成 servlet。shell 里 `review` 走审稿，`x` 是框架自带命令。日志人格是 `starwars`。

属性示例在 [`application-local.yml.example`](dream-embabel-bootstrap/src/main/resources/application-local.yml.example)。复制成 `application-local.yml` 后不要提交。

### 页面

另开一个终端：

```text
cd dream-embabel-ui
npm install
npm run dev
```

浏览器打开 `http://127.0.0.1:5174`。`/api` 代理到 `http://127.0.0.1:8080`。Java 改完要重启进程，页面才会打到新接口。

演示页提交后：`COMPLETED` 为 HTTP 200，`WAITING` 为 202，其它进程状态为 409。空白内容为 400。没有对话模型，或 echo 工具没打开，为 503。

| 页面 | 路由 | 调用 |
|---|---|---|
| 审稿 | `/reviews` | `POST /api/reviews/runs`，正文 `{"topic"}`。进度 `GET /api/reviews/runs/{runId}`。确认 `POST /api/reviews/runs/{runId}/confirmations`。一次性调用仍是 `POST /api/reviews` |
| 短回答 | `/replies` | `POST /api/replies`，`{"message"}` |
| 分支 | `/branches` | `POST /api/branches`，`{"text"}` |
| 自动 | `/auto` | `POST /api/auto-replies`，`{"message"}` |
| 纯文本 | `/text` | `POST /api/text-lines`，`{"message"}` |
| 抽取 | `/extract` | `POST /api/maybe-notes`，`{"message"}` |
| 重规划 | `/replan` | `POST /api/replans`，`{"text"}` |
| 效用 | `/utility` | `POST /api/utilities`，`{"text"}` |
| 监督 | `/stages` | `POST /api/stages`，`{"text"}` |
| 反思 | `/reflect` | `POST /api/reflections`，`{"text"}` |
| 日志 | `/logs` | `POST /api/named-lines`，`{"text"}` |
| 工具组 | `/math` | `GET /api/math-sums/tools`，计算 `POST /api/math-sums`，`{"text"}` |
| 函数工具 | `/stamps` | `POST /api/stamps`，`{"text"}` |
| 展开 | `/unfold` | `POST /api/unfolds`，`{"text"}` |
| 门禁 | `/gates` | `POST /api/gates`，`{"text"}` |
| 角色 | `/roles` | `POST /api/roles`，`{"text"}` |
| 兜底 | `/fallbacks` | `POST /api/fallbacks`，`{"text"}` |
| 分发 | `/scatters` | `POST /api/scatters`，`{"text"}` |
| 共识 | `/consensus` | `POST /api/consensus`，`{"text"}` |
| 引用 | `/guide-lines` | `GET /api/guide-lines/tools`，写一句 `POST /api/guide-lines`，`{"text"}` |
| 预取 | `/prefetches` | `POST /api/prefetches`，`{"text"}` |
| 过滤 | `/filters` | `POST /api/filters`，`{"text"}` |
| 原文 | `/urtexts` | `POST /api/urtexts`，`{"text"}` |
| 记忆 | `/memories` | `POST /api/memories`，`{"text"}` |
| 简式 | `/simple-lines` | `POST /api/simple-lines`，`{"text"}` |
| 表单 | `/forms` | `POST /api/forms` 停住，再 `POST /api/forms/submit`，`{"processId","column","point"}` |
| 子代理 | `/subagents` | `POST /api/subagents`，`{"text"}` |
| 导出 | `/exports` | `POST /api/exports`，`{"text"}` |
| 回声 | `/echoes` | `POST /api/echoes`，`{"text"}` |
| 规范 | `/style-guide` | `GET /api/style-guide`，检索 `POST /api/style-guide/searches`，`{"query"}`。这一页不调用模型 |
| 进程 | `/processes` | `GET /api/processes` |
| MCP | `/mcp` | `GET /api/mcp`，列工具 `POST /api/mcp/tools` |

## 架构

包名 `com.zhu.embabel`，`groupId` 为 `com.zhu`。

- `dream-embabel-bom`：版本与 dependencyManagement。
- `dream-embabel-domain`：领域 record 和 `@Agent`。依赖 `embabel-agent-api`、`embabel-agent-rag-lucene`、`embabel-agent-rag-tika`。不依赖 Spring Web，也不依赖模型 starter。
- `dream-embabel-adapter`：OpenAI 与 DeepSeek 的模型配置，以及启动时的模型探测。没有 key 时不加载 OpenAI 自动配置，改为占位模型 `local`。
- `dream-embabel-bootstrap`：启动类、shell 命令 `review`、各演示页的 HTTP、Milvus、echo MCP、进程快照。
- `dream-embabel-ui`：Vue 页面。每个能力一条路由。

演示页的共同路径是：domain 里的 `@Agent`，bootstrap 里 `AgentRuns` 做空白检查和一次 `AgentInvocation`，Controller 用 `RunStatus` 把进程状态映射成 HTTP 状态码。写一句的共同收尾在 `Prompts`，各 Agent 只保留自己的要求。

### 审稿

`ContentReviewAgent` 的动作链没有手写顺序。规划器排出：

`draftContent` → `acceptDraft` → `reviewDraft` → `finalize`

| 动作 | 读取 | 产出 |
|---|---|---|
| `draftContent` | `UserInput` | `Draft` |
| `acceptDraft` | `Draft` | `AcceptedDraft` |
| `reviewDraft` | `UserInput`、`Draft`、`AcceptedDraft` | `Review` |
| `finalize` | 用户输入、初稿、审校 | `FinalArticle` |

`finalize` 带 `@AchievesGoal`。对外导出名是 `reviewAndFinalize`，`remote = true`，`startingInputTypes` 是 `UserInput`。HTTP 和 shell 按结果类型 `FinalArticle` 调用，不靠这个名字。

字数和确认的当前取值：

```properties
dreamembabel.review.draft-words=400
dreamembabel.review.review-words=200
dreamembabel.review.final-words=500
dreamembabel.review.min-score=60
dreamembabel.review.confirm=true
```

`confirm=true` 时，`acceptDraft` 调用 `WaitKt.confirm`，进程进入 `WAITING`。页面走 `/api/reviews/runs` 看到进度后再提交确认。shell 的 `review` 发现进程在等待时只返回提示。

`ReviewMemory` 记下最近一次审校。下一轮 `draftContent` 会把它写进 prompt。

创作和定稿通过 `withToolObject` 挂上本地工具 `houseStyle`。审校不挂这个工具。创作、审校和定稿都挂上空白主题护栏。

| 步骤 | 角色 | 温度 |
|---|---|---|
| 创作 | `writer` | `0.7` |
| 审校 | `reviewer` | `0.2` |
| 定稿改写 | `writer` | 不设 |

角色对应的模型名是 `embabel.models.llms.writer` 和 `embabel.models.llms.reviewer`。

审校分数低于 `dreamembabel.review.min-score`（当前 60）时，动作抛出 `ReplanRequestedException`，并藏起初稿和已确认初稿。`draftContent` 与 `acceptDraft` 允许重跑。

定稿内部再用 `RepeatUntilAcceptableBuilder` 改写，最多 3 轮。打分用 `TextFeedback` 的 0 到 1，通过线是 `min-score / 100`（当前 0.60）。这一步不代替审校上的重规划。

规划日志的 logger 名是 `com.zhu.embabel.plan`，默认 INFO。格式：

```text
plan process=<进程id> seq=1 action=draftContent in=UserInput out=Draft
run process=<进程id> seq=1 action=draftContent
```

[docs/v0.1-run-sample.md](docs/v0.1-run-sample.md) 仍是占位，里面没有真实终端输出。

### 栏目规范

规范是 classpath 上的 Markdown，经 Tika 解析后写入进程内 Lucene。`AddTitlesChunkTransformer` 把文档标题和章节标题放进 chunk。引用名是 `styleGuide`，模型看到的工具名带这个前缀，例如 `styleguide_textSearch`、`styleguide_vectorSearch`、`styleguide_broadenChunk`、`styleguide_zoomOut`。

`dreamembabel.rag.milvus.enabled=true` 时，向量检索走 Milvus，全文和相邻片段仍走 Lucene。规范页只检索，不调用模型。

### MCP

- echo 当前是打开的。创作和定稿因此多一个 `echo` 工具。回声页要求模型先调用它。
- 设置 `dreamembabel.mcp.remote.url` 后，再挂上该根地址的 Streamable HTTP 工具。请求发到 `{url}` 下的 endpoint，默认 `/mcp`。不设置这个地址就不会连接。

## 测试

`mvnw test` 和 `mvnw verify` 都不访问模型。

- domain 单测用 `FakeOperationContext`。`expectResponse` 返回假对象，再用 `getLlmInvocations()` 检查 prompt 和调用次数。
- `ContentReviewAgentIntegrationTest` 被 surefire 排除，不在 `verify` 里。它会拉起整个 Spring。

## 当前边界

没有 Redis，也没有 Docker 编排。页面不在 Maven reactor 里，CI 也不执行 `vite build`。

## 贡献

见 [CONTRIBUTING.md](CONTRIBUTING.md)。提缺陷或改动时使用仓库里的 Issue、PR 模板。不要把密钥写进说明。

## 许可与声明

Apache-2.0，见 [LICENSE](LICENSE)。再分发时保留本仓库的 [NOTICE](NOTICE)，并保留 Embabel、Spring Boot、Spring AI 各自发行包中的版权与 NOTICE。这些依赖同样是 Apache-2.0。

本仓库是示例项目。调用模型产生的费用由调用方承担。
