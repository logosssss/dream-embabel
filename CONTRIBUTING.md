# 贡献

本仓库是 Embabel GOAP 的示例基座。改动保持在现有模块里：领域动作在 `dream-embabel-domain`，模型配置在 `dream-embabel-adapter`，启动和 shell 在 `dream-embabel-bootstrap`。

需要 JDK 21。无模型 key 时运行：

```text
mvnw -B test -DskipTests=false
```

测试不得访问真实模型、外部向量库或外部 MCP 服务。不要提交密钥、`.env` 或 `application-local.yml`。

提 Issue 或 Pull Request 时使用仓库模板，并在说明里写清 JDK 版本和有没有设置 key。
