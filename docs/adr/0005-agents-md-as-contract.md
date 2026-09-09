# ADR 0005: AGENTS.md 作为可执行契约，不是文档

- 状态：Accepted
- 日期：2026-09-07

## 背景

市面上的 AGENTS.md 都是散文，写完就开始腐烂——没有任何机制保证代码遵守它，也没有任何机制保证文档本身不过期。

## 决定

把 AGENTS.md 做成**可执行契约**：

1. 规则分两栏：`[machine]`（有自动化检查背书）与 `[judgment]`（带理由的人/agent 判断）。
2. 每条 `[machine]` 规则用 `— detekt:X` / `— konsist:Y` / `— task:Z` / `— ci:W` 声明它的检查。
3. 自研 `tools/agent-contract` Gradle 插件在 CI 里跑 `verifyAgentContract`：验证每条声明的检查真实存在（detekt 配置里有该规则且 active、Konsist 测试类存在、Gradle task 存在、CI 工作流里有该步骤）。
4. 产出"规范覆盖率"徽章：多少条 machine 规则真的被强制执行，是一个可量化、可晒的指标。

## 理由

- 文档与工具链的一致性测试，使"规范永不腐烂"从愿望变成可证明的事实。
- 判断性规则必须带理由——理由是让 agent（和人）正确泛化的唯一途径。
- 这是项目区别于 nowinandroid 式模板的核心差异点。

## 代价

- 每加一条 machine 规则，必须先实现检查——这是特性，不是成本。

## 推翻条件

社区出现更成熟的 AGENTS.md 校验标准时，迁移过去。
