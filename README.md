# dream-embabel

基于 [Embabel](https://github.com/embabel/embabel-agent) 的 Java Agent 工程化基座。v0.1 跑通 GOAP：领域类型决定动作顺序，规划器用前置条件和效果做确定性搜索，开发者不手写「先 A 再 B」。

同定位的另外两个仓库：

- [dream-ai](https://github.com/logosssss/dream-ai)：Spring Boot 上的模块化单体。对话走工具循环，图编排用 Spring AI Alibaba 的 StateGraph。
- [dream-scope](https://github.com/logosssss/dream-scope)：基于 AgentScope Java 2.0 的模块化单体。同步和流式入口都走 HarnessAgent。
- dream-embabel（本仓库）：用 GOAP 做目标驱动的确定性规划。

## 版本

| 项 | 版本 |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Spring Boot | 4.1.1 |
| Embabel | 1.5.3 |
| Spring AI | 2.0.1，由 Embabel 传递，本仓库不写版本号 |

核实记录见 [docs/v0.1-verification.md](docs/v0.1-verification.md)。

## 快速开始

需要 JDK 21。仓库带 Maven Wrapper（3.9.11）：Windows 用 `mvnw.cmd`，其他系统用 `./mvnw`。仓库里没有密钥。

无 key 也可以启动。进程使用占位模型 `local`，shell 能起来；真正调用模型会提示先设置 key。

```text
mvnw -B test -DskipTests=false
```

有 OpenAI key 时，设置 `OPENAI_API_KEY` 后启动。进程会打开 Spring profile `openai`，默认模型是 `gpt-4.1-mini`。不需要再开 Maven profile。

```text
mvnw -pl dream-embabel-bootstrap -am spring-boot:run
```

shell 里：

```text
x "写一篇关于春天的短文"
review 写一篇关于春天的短文
```

`x` 是框架自带命令。`review` 和 `POST /api/reviews` 都用 `AgentInvocation.create(agentPlatform, FinalArticle.class)` 做编程式调用。

```text
POST http://localhost:8080/api/reviews
Content-Type: application/json

{"topic":"写一篇关于春天的短文"}
```

完成后 HTTP 200，正文在 `title`、`body`、`content`。初稿等待确认时 HTTP 202，`status` 为 `WAITING`。主题为空白时 HTTP 400。

Embabel shell 默认不启动 Web。`application.properties` 里 `embabel.agent.shell.web-application-type=servlet` 才会让 Tomcat 监听 8080。重启后日志里应出现 Tomcat 已在 8080 启动。

DeepSeek 设置 `DEEPSEEK_API_KEY` 后启动。profile 为 `deepseek`，base 为 `https://api.deepseek.com`，模型为 `deepseek-flash`（2026-10-22 核对）。两把 key 同时存在时用 DeepSeek。

属性示例在 `dream-embabel-bootstrap/src/main/resources/application-local.yml.example`。复制成 `application-local.yml` 后不要提交。

字数和确认的默认值：

```properties
dreamembabel.review.draft-words=400
dreamembabel.review.review-words=200
dreamembabel.review.final-words=500
dreamembabel.review.min-score=60
dreamembabel.review.confirm=false
```

`dreamembabel.review.confirm=true` 时，初稿确认会停住等回复。

- `x` 由 shell 接收确认。
- `review` 发现进程在等待时只返回提示，不替你回复。

规划日志的 logger 名是 `com.zhu.embabel.plan`，默认 INFO。

## 架构

包名 `com.zhu.embabel`，`groupId` 为 `com.zhu`。

领域类型和 `@Action` 放在一起，动作之间靠 record 类型推导。domain 依赖 `embabel-agent-api`，再依赖 `embabel-agent-rag-lucene` 和 `embabel-agent-rag-tika`。它不依赖 Spring Web，也不依赖模型 starter。

模块：

- `dream-embabel-bom`：版本与 dependencyManagement。
- `dream-embabel-domain`：领域 record、Persona、`ContentReviewAgent`、进程内 Lucene。
- `dream-embabel-adapter`：OpenAI 与 DeepSeek 的模型配置。`embabel-agent-starter-openai` 在 adapter 里是 optional，由 bootstrap 直接依赖。没有 key 时不加载 OpenAI 自动配置。
- `dream-embabel-bootstrap`：启动类、shell 命令、`POST /api/reviews`、规划日志。

动作链没有手写顺序。页面走 `POST /api/reviews`，shell 的 `review` 走同一次调用。规划器排出：

`draftContent` → `acceptDraft` → `reviewDraft` → `finalize`

| 动作 | 读取 | 产出 |
|---|---|---|
| `draftContent` | `UserInput` | `Draft` |
| `acceptDraft` | `Draft` | `AcceptedDraft` |
| `reviewDraft` | `UserInput`、`Draft`、`AcceptedDraft` | `Review` |
| `finalize` | 用户输入、初稿、审校 | `FinalArticle` |

`finalize` 带 `@AchievesGoal`。对外导出名是 `reviewAndFinalize`，`remote = true`，`startingInputTypes` 是 `UserInput`。

### 确认与记忆

确认默认关闭，`acceptDraft` 直接转换初稿。打开 `dreamembabel.review.confirm` 后，这一步调用 `WaitKt.confirm`，进程进入 `WAITING`。

`ReviewMemory` 记下最近一次审校。下一轮 `draftContent` 会把它写进 prompt。

### 栏目规范

规范是 classpath 上的 Markdown，经 Tika 解析后写入进程内 Lucene。`AddTitlesChunkTransformer` 把文档标题和章节标题放进 chunk。

`ToolishRag` 用 `SearchDefaults` 的全文阈值（0），经 `withReference` 挂到创作、审校和定稿。模型可以：

- `textSearch` 查规范
- `broadenChunk` 看相邻片段
- `zoomOut` 看父节

全文检索不访问模型。向量用字符哈希，不接外部向量库。

### 本地工具与 MCP

创作和定稿通过 `withToolObject` 挂上本地工具 `houseStyle`（`@LlmTool`）。审校不挂这个工具。

- `dreamembabel.mcp.echo.enabled=true` 时，用 stdio 拉起本机 `EchoMcpServer`。创作和定稿多一个 `echo` 工具。默认关闭，测试不会为它再起一个进程。
- 设置 `dreamembabel.mcp.remote.url` 后，创作和定稿再挂上该根地址的 Streamable HTTP 工具。请求发到 `{url}` 下的 endpoint，默认 `/mcp`。默认不设置这个地址，测试和启动都不连外网。

### 模型、护栏与返修

创作、审校和定稿都挂上 `UserInputGuardRail` `topicPresent`。用户输入是空白时，这次模型调用被拒绝。

角色用 `LlmOptions.withLlmForRole`。对应的模型名写在 `embabel.models.llms.writer` 和 `embabel.models.llms.reviewer`。

| 步骤 | 角色 | 温度 |
|---|---|---|
| 创作 | `writer` | `0.7` |
| 审校 | `reviewer` | `0.2` |
| 定稿改写 | `writer` | 不设 |

审校用 `creating(Review.class).fromPrompt(...)`。分数低于 `dreamembabel.review.min-score`（默认 60）时，动作抛出 `ReplanRequestedException`，并 `hide` 掉初稿和已确认初稿。`draftContent` 与 `acceptDraft` 允许重跑。

定稿动作内部再用 `RepeatUntilAcceptableBuilder` 改写，最多 3 轮。打分用 `TextFeedback` 的 0 到 1，通过线是 `min-score / 100`（默认 0.60）。这一步不代替审校上的重规划。

### 规划日志

规划监听是公开的 `AgenticEventListener`。日志格式：

```text
plan process=<进程id> seq=1 action=draftContent in=UserInput out=Draft
plan process=<进程id> seq=2 action=acceptDraft in=Draft out=AcceptedDraft
plan process=<进程id> seq=3 action=reviewDraft in=AcceptedDraft,Draft,UserInput out=Review
plan process=<进程id> seq=4 action=finalize in=Draft,Review,UserInput out=FinalArticle
run process=<进程id> seq=1 action=draftContent
run process=<进程id> seq=2 action=acceptDraft
run process=<进程id> seq=3 action=reviewDraft
run process=<进程id> seq=4 action=finalize
```

真实终端输出待有 key 后补到 [docs/v0.1-run-sample.md](docs/v0.1-run-sample.md)。

## 测试

`mvn install` 默认跳过测试。要执行测试：`mvn test -DskipTests=false` 或 `mvn verify -DskipTests=false`。这两条都不访问模型。

- 单测在 domain。`FakeOperationContext` 注入 `Ai`，`expectResponse` 返回假对象，再用 `getLlmInvocations()` 检查 prompt、调用次数和温度。
- 集成测试在 bootstrap，继承 `EmbabelMockitoIntegrationTest`。四次 `whenCreateObject` 打桩后，用 `AgentInvocation` 跑完整链路。定稿打分返回 0.8，高于默认通过线 0.60，循环只走一轮。基类把默认模型设为 `test-model`，测试里注册同名 `LlmService`，否则框架因零模型无法启动。

## Roadmap

已做：

- 模型分级（`writer` / `reviewer`）
- 低分重规划，以及定稿内的 `RepeatUntilAcceptableBuilder`（最多 3 轮）
- 人工确认（默认关）和最近一次审校记忆
- 本地工具 `houseStyle`
- 进程内 Lucene `ToolishRag`：Tika 解析 Markdown、标题写入 chunk、`SearchDefaults`、`broadenChunk` / `zoomOut`
- 本机 stdio echo MCP（默认关）
- 按地址连接的 Streamable HTTP MCP（默认不设置地址）
- 空白主题护栏 `topicPresent`
- 对外目标 `reviewAndFinalize`
- 开源门面：Apache-2.0、NOTICE、贡献说明、Issue 与 PR 模板

仍不做 Web、数据库、外部向量库、Redis、Docker 编排和多 Agent 协作。

## 贡献

见 [CONTRIBUTING.md](CONTRIBUTING.md)。提缺陷或改动时使用仓库里的 Issue、PR 模板。不要把密钥写进说明。

## 许可与声明

Apache-2.0，见 [LICENSE](LICENSE)。再分发时保留本仓库的 [NOTICE](NOTICE)，并保留 Embabel、Spring Boot、Spring AI 各自发行包中的版权与 NOTICE。这些依赖同样是 Apache-2.0。

本仓库是示例项目。调用模型产生的费用由调用方承担。
