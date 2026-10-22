# dream-embabel

基于 [Embabel](https://github.com/embabel/embabel-agent) 的 Java Agent 工程化基座。v0.1 跑通 GOAP：领域类型决定动作顺序，规划器用前置条件和效果做确定性搜索，开发者不手写「先 A 再 B」。

同定位的另外两个仓库：

- [dream-ai](https://github.com/logosssss/dream-ai)：手写 Agent 运行时，不依赖编排框架。
- [dream-scope](https://github.com/logosssss/dream-scope)：用 StateGraph 固定图编排。
- dream-embabel（本仓库）：用 GOAP 做目标驱动的确定性规划。

本期范围以本说明为准。`docs/spec.md` 是更早的 HTTP 对照稿，不属于 v0.1。

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

需要 JDK 21。仓库带 Maven Wrapper（3.9.11），Windows 用 `mvnw.cmd`，其他系统用 `./mvnw`。仓库里没有密钥。

无 key 时只跑测试，不要启动应用。Embabel 在零模型时会拒绝启动，报错 `No models detected`。

```text
mvnw -B test
```

有 OpenAI key 时只设置 `OPENAI_API_KEY`，再编译启动。Maven profile `openai-models` 会带上 `embabel-agent-starter-openai`，Spring profile 变为 `openai`，默认模型 `gpt-4.1-mini`。

```text
mvnw -pl dream-embabel-bootstrap -am spring-boot:run
```

shell 里：

```text
x "写一篇关于春天的短文"
review 写一篇关于春天的短文
```

`x` 是框架自带命令。`review` 用 `AgentInvocation.create(agentPlatform, FinalArticle.class)` 做编程式调用。

DeepSeek 只设置 `DEEPSEEK_API_KEY`，不要和 `OPENAI_API_KEY` 同时设置。后声明的 Maven profile 会盖住 Spring profile 名。base 为 `https://api.deepseek.com`，模型为 `deepseek-flash`（2026-10-22 核对）。属性示例在 `dream-embabel-bootstrap/src/main/resources/application-local.yml.example`，复制成 `application-local.yml` 后不要提交。

字数默认值：

```properties
dreamembabel.review.draft-words=400
dreamembabel.review.review-words=200
dreamembabel.review.final-words=500
```

规划日志的 logger 名是 `com.zhu.embabel.plan`，默认 INFO。

## 架构

模块：

- `dream-embabel-bom`：版本与 dependencyManagement。
- `dream-embabel-domain`：领域 record、Persona、`ContentReviewAgent`。只依赖 `embabel-agent-api`。
- `dream-embabel-adapter`：OpenAI 与 DeepSeek 的模型配置。`embabel-agent-starter-openai` 为 optional，运行时由 bootstrap 的 Maven profile 引入。
- `dream-embabel-bootstrap`：启动类、shell 命令、规划日志。

包名 `com.zhu.embabel`，`groupId` 为 `com.zhu`。

架构取舍：领域类型和 `@Action` 要放在一起，动作之间靠 record 类型推导。domain 因此依赖 `embabel-agent-api`，不依赖 Spring Web，也不依赖模型 starter。

动作链没有手写顺序。起点是 `UserInput`。`draftContent` 产出 `Draft`。`reviewDraft` 读取 `UserInput` 和 `Draft`，产出 `Review`。`finalize` 读取这三者，带 `@AchievesGoal` 与 `@Export(remote = true, name = "reviewAndFinalize")`。规划器据此排出 `draftContent` → `reviewDraft` → `finalize`。

创作使用 `LlmOptions.withAutoLlm().withTemperature(0.7)` 和 `RoleGoalBackstory`。审校使用温度 `0.2` 和 `Persona`，并用 `creating(Review.class).fromPrompt(...)`。定稿使用 `withAutoLlm()`。

规划监听是公开的 `AgenticEventListener`，不是反射。日志格式：

```text
plan process=<进程id> seq=1 action=draftContent in=UserInput out=Draft
plan process=<进程id> seq=2 action=reviewDraft in=Draft,UserInput out=Review
plan process=<进程id> seq=3 action=finalize in=Draft,Review,UserInput out=FinalArticle
run process=<进程id> seq=1 action=draftContent
run process=<进程id> seq=2 action=reviewDraft
run process=<进程id> seq=3 action=finalize
```

真实终端输出待有 key 后补到 [docs/v0.1-run-sample.md](docs/v0.1-run-sample.md)。

## 测试

`mvn test` 和 `mvn verify` 不访问模型。

- 单测在 domain：`FakeOperationContext` 注入 `Ai`，`expectResponse` 返回假对象，再用 `getLlmInvocations()` 检查 prompt、调用次数和温度。
- 集成测试在 bootstrap：继承 `EmbabelMockitoIntegrationTest`，三次 `whenCreateObject` 打桩后用 `AgentInvocation` 跑完整链路。基类把默认模型设为 `test-model`，测试里注册同名 `LlmService`，否则框架因零模型无法启动。

## Roadmap

v0.2：动态重规划、模型分级、领域记忆、人工确认。

v1.0：Agentic RAG、MCP、面向外部的开源门面。

v0.1 不做 Web、数据库、向量库、RAG、MCP、人工确认、Redis、Docker 编排、多 Agent 协作和动态重规划演示。

## 许可与声明

Apache-2.0，见 [LICENSE](LICENSE)。再分发时保留本仓库的 [NOTICE](NOTICE)，并保留 Embabel、Spring Boot、Spring AI 各自发行包中的版权与 NOTICE。这些依赖同样是 Apache-2.0。

本仓库是示例项目。调用模型产生的费用由调用方承担。
