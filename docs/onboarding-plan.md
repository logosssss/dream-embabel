# 从零上手规划

给第一次把 Embabel 1.5.3 接到 Spring Boot 上的人。版本以本仓库为准：JDK 21、Maven 3.9+、Spring Boot 4.1.1、Spring AI 2.0.1（Embabel 传递，不要另写版本）。

本仓库已经走过后面几个阶段。对照即可，不必把模块推倒重来。事实核对见 [v0.1-verification.md](v0.1-verification.md)，启动命令见仓库根目录 README。想按概念学框架本身，看 [learning-path.md](learning-path.md)。

动手前先定产品。交一个主题、拿回审过的成稿，用多步 Agent，一次请求会串行打多次模型。一句进、一句出，用一个动作，一次模型调用。两条都能落在这个框架上。不要第一天就做审稿链。

## 阶段一：工程能起来

固定四个模块，不要再拆一个 web 模块。

| 模块 | 放什么 |
|---|---|
| `dream-embabel-bom` | 父 POM、Embabel 版本、dependencyManagement |
| `dream-embabel-domain` | 领域 record 和 `@Agent`。不依赖 Web，不依赖模型 starter |
| `dream-embabel-adapter` | 某个供应商的模型登记，例如 DeepSeek 的 OpenAI 兼容模型 |
| `dream-embabel-bootstrap` | 启动类、shell、HTTP、按环境变量打开 profile |

Windows 上先确认 `java -version` 是 21。PATH 上若仍是 JDK 8，把 `JAVA_HOME` 指到 JDK 21 再用仓库里的 `mvnw.cmd`。

完成标准：不设置任何 key，进程能启动。日志里默认模型是占位名 `local`。模型列表为空时，框架会在启动期拒绝，所以无 key 也要登记一个占位模型。

这一阶段不要做的事：不接真实模型，不写审稿动作，不引入数据库和外部向量库。

## 阶段二：只做一个动作

在 domain 放一个 record，例如 `QuickReply(String reply)`，再放一个只有一个 `@Action` 的 Agent。动作用 `creating(QuickReply.class).fromPrompt(...)`，走默认模型。不挂工具，不挂检索，不设角色。输出用 `withMaxTokens` 封顶，避免一次对话被写成一篇稿。

测试用 `FakeOperationContext`。`expectResponse` 给出假对象，再断言 `getLlmInvocations()` 只有一次，方法是 `CREATE_OBJECT`，工具和护栏都是空的。这条测试不访问网络。

完成标准：`mvnw -pl dream-embabel-domain test -DskipTests=false -Dtest=QuickReplyAgentTest` 通过。父 POM 默认 `skipTests=true`，所以要显式打开。

这一阶段不要做的事：不写第二个动作，不把规范检索写进提示词。提示词里写了「调用某工具」，模型就会多一轮，本地工具也一样。

## 阶段三：让这一次调用打到真实模型

key 放进程环境变量，不写进仓库。

- DeepSeek 用 `DEEPSEEK_API_KEY`。两把 key 都在时用 DeepSeek。
- OpenAI 用 `OPENAI_API_KEY`。
- IDEA 运行配置里的「环境变量」会进入这一次进程。改完要重新运行。写在 VM options 的 `-D` 里不会被这里读到。
- 已经打开的 IDE 看不到之后才加的 Windows 用户变量。要完全退出 IDE 再打开。

启动早期用 `EnvironmentPostProcessor` 看 `System.getenv`，有 key 就 `addActiveProfile`。Boot 4.1 只从 `META-INF/spring.factories` 加载这个类，键是 `org.springframework.boot.EnvironmentPostProcessor`。写到 `META-INF/spring/` 下的同名文件不会生效。

profile 打开之后，才加载 `application-deepseek.properties` 或 `application-openai.properties`。文件里的 `api-key=${DEEPSEEK_API_KEY}` 是给客户端用的，不能反过来打开 profile。

占位模型和真实模型都必须是 `SpringAiLlmService`。自写一个 `LlmService` 时，生成文本会在调用前被拒绝。无 key 的占位 `ChatModel` 直接抛出明确错误，HTTP 层把它收成 503。

用到 `writer`、`reviewer` 时，把 `embabel.models.llms.writer` 和 `embabel.models.llms.reviewer` 指到已经登记的模型名。只配默认模型，角色仍然找不到。

完成标准：重启后日志出现 profile `deepseek` 或 `openai`，默认模型不再是 `local`。

## 阶段四：从进程外面打这一次调用

HTTP 放在 bootstrap，和 shell 共用 `AgentInvocation`。

Embabel shell 默认把 Web 应用类型设成 `none`，`application.properties` 里写 `spring.main.web-application-type` 盖不过它。要 Tomcat 听 8080，设置：

```properties
embabel.agent.shell.web-application-type=servlet
```

依赖用 `spring-boot-starter-webmvc`。接口先做短回答：`POST /api/replies`，正文 `{"message":"..."}`。空白内容返回 400。没有真实模型返回 503。成功返回 200，`status` 为 `COMPLETED`。

完成标准：用 Apifox 或 curl 打通，耗时接近一次模型补全。这是后面审稿接口的对照。

## 阶段五：再做多步审稿

类型先写出来，顺序不要手写。规划器按前置类型搜索：

| 动作 | 读取 | 产出 |
|---|---|---|
| `draftContent` | `UserInput` | `Draft` |
| `acceptDraft` | `Draft` | `AcceptedDraft` |
| `reviewDraft` | 用户输入、初稿、已接受初稿 | `Review` |
| `finalize` | 用户输入、初稿、审校 | `FinalArticle` |

建议仍按这个顺序加上能力，每加一项就打一次 `POST /api/reviews`，看耗时增加是否说得通。

1. 四个动作都只做 `creating`，不挂工具。顺利路径是四次模型调用：初稿、审校、改写、打分。
2. 审校分数低于 `dreamembabel.review.min-score`（默认 60）时抛 `ReplanRequestedException`，并藏起初稿。这会把初稿和审校再跑一遍。
3. 定稿内部用 `RepeatUntilAcceptableBuilder`，最多 3 轮，通过线是 `min-score / 100`（默认 0.60）。这一步不代替审校上的重规划。
4. 最后才加 `houseStyle`、Lucene 规范和人工确认。确认默认关。打开 `dreamembabel.review.confirm` 后，进程会停在 `WAITING`，HTTP 返回 202，确认由 shell 的 `x` 接收。

`POST /api/reviews` 的正文是 `{"topic":"写一篇关于春天的短文"}`。`topic` 是文章主题，不是魔法口令。

完成标准：规划日志（logger 名 `com.zhu.embabel.plan`）出现 `draftContent → acceptDraft → reviewDraft → finalize`。一次成功请求的等待，大致是模型次数乘以单次延迟。本机 2026-10-23 的一次成稿约 90 秒，响应大约 2 KB。

## 阶段六：把测试边界守住

| 测试 | 放哪 | 规则 |
|---|---|---|
| 动作的 prompt、温度、有没有工具 | domain，`FakeOperationContext` | 不启动 Spring，不访问模型 |
| shell / HTTP 整段流程 | bootstrap，继承 `EmbabelMockitoIntegrationTest` | `whenCreateObject` 打桩。基类默认模型名是 `test-model`，测试要注册同名 `LlmService`，provider 不要写成 `local` |
| 真实 key | 只有人手动打接口 | 不进 CI，不进仓库 |

集成测试在静态块里设置 `dreamembabel.models.auto-profile=false`，而且要在 Spring 启动前执行。`@TestPropertySource` 来不及挡住按环境变量打开 profile。

Maven 在某个模块没有匹配测试时会让整个反应器失败。只跑一个测试类时加上 `-Dsurefire.failIfNoSpecifiedTests=false`。PowerShell 里 `-D` 参数要加引号。

## 推荐顺序

| 顺序 | 做完能看到什么 | 先不要做 |
|---|---|---|
| 1 | 无 key 启动，模型名是 `local` | 真实 key、页面 |
| 2 | 单测证明只有一次模型调用 | 第二个动作 |
| 3 | 日志里是 `deepseek` 或 `openai` | 把 key 写进 properties |
| 4 | `POST /api/replies` 返回短句 | 审稿、工具、检索 |
| 5 | `POST /api/reviews` 返回定稿，并接受它更慢 | 数据库、外部向量库、多 Agent |
| 6 | 单测和打桩集成测试都不访问模型 | 用生产 key 跑 CI |

页面是这套 HTTP 的另一个进程：提交 `topic` 或 `message`，把返回的字符串渲染出来。shell 不是给前端用的通道。
