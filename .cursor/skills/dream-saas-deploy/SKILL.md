---
name: dream-saas-deploy
description: >-
  Deploy Dream-SaaS to production (47.119.115.162 / dream-saas.com). Use when the
  user asks to deploy, 上线, 发到现网, 部署生产, deploy frontend/gateway/admin/full,
  or ship to the live server. Always run deploy/scripts/agent-deploy.ps1 — never
  improvise scp/docker/mvn-on-server sequences.
---

# Dream-SaaS 现网部署 Skill

## 硬性入口

用户要求现网部署时，**只执行**：

```powershell
.\deploy\scripts\agent-deploy.ps1 -Target <frontend|ai-gateway|saas-gateway|admin|full>
```

在仓库根目录运行。未指定目标时用 `full`，但若用户只改了前端/某一网关，选对应 Target。

## 禁止

- 手写乱序：`scp` JAR、`docker compose build`、`remote-redeploy-docker-one`、服务器 `mvn package`
- 跳过 Gate / 失败后自动换另一套命令重试
- 未过健康检查或工作台路径冒烟宣称成功
- 整文件覆盖 `dream-saas-ssl.conf`
- 擅自 `git commit` / `git push`（除非用户另提）

## 失败时

脚本已失败即停并打印「只看」日志命令。Agent **只读该提示**，修好后**再次调用同一脚本**（可加 `-SkipBuild` / `-SkipFrontend` 等参数），不要另起炉灶。

## 参考

- `deploy/AGENT-DEPLOY-USAGE.md` — **使用与验证（优先）**
- `deploy/AGENT-DEPLOY.md` — 关卡说明
- `deploy/PROD-OPS-GUIDE.md` — 现网长文
- 搭子专项：走 `companion-cloud-deploy.ps1`，不要硬套 `full`

## 结束汇报

使用 AGENT-DEPLOY 模板：目标 / Gate / 产物 / 远端 / 健康检查 / 回滚。
