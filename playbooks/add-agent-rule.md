# Playbook: 新增一条规范（add-agent-rule）

规范的生命周期：**能机器化就机器化，不能机器化就写清理由。**

## 加一条 [machine] 规则

1. 先实现检查本身：
   - 静态代码规则 → `tools/detekt-rules/` 新增 Rule + 注册到 `CharterRuleSetProvider` + `config/detekt/detekt.yml` 激活。
   - 结构/依赖规则 → `architecture-tests/` 新增 Konsist 测试类。
   - 流程规则 → Gradle task 或 CI 步骤。
2. 在根 AGENTS.md（或模块 AGENTS.md）写规则，格式：
   `- [machine] 规则文本。 — detekt:RuleId`（或 `konsist:ClassName` / `task:taskName` / `ci:needle`）
3. 跑 `./gradlew verifyAgentContract`：它会验证声明的检查真实存在。这一步挂了说明检查和声明对不上——修检查或改声明，不许绕过。
4. 提交检查实现 + 文档声明在同一 PR。

## 加一条 [judgment] 规则

1. 写清楚规则和**理由**。没有理由的判断规则会被删掉——理由是让 agent 泛化的关键。
2. 格式：`- [judgment] 规则文本。 — reason: 为什么。`
3. 定期评审：当某条 judgment 规则可以机器化时，机器化并改标签。

## 禁止

- 禁止给不存在的检查打 [machine] 标签（verifyAgentContract 会挂，这正是设计意图）。
- 禁止把可以机器化的规则留在 [judgment] 里（那是规范腐烂的入口）。
