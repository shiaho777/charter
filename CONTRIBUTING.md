# Contributing

## 环境

- JDK 21
- Android SDK（API 36+）
- 本仓库不检查 `local.properties`——自己创建并指向你的 SDK：
  ```
  sdk.dir=/path/to/Android/sdk
  ```

## 在改任何东西之前

1. 读根 `AGENTS.md`。它是宪法，也是可执行契约。
2. 改哪个模块，读哪个模块的 `AGENTS.md`。
3. 重复性任务先查 `playbooks/` 有没有标准流程。

## 提交要求

- Conventional Commits：`feat:` / `fix:` / `refactor:` / `docs:` / `test:` / `build:`。
- 每个 PR 必须通过 `./gradlew verifyUi`。
- UI 变更必须附截图（明暗双主题，关键状态）。
- 改了 AGENTS.md 的 `[machine]` 规则？必须先实现检查，再改文档，同一 PR 提交。

## 加规则

见 `playbooks/add-agent-rule.md`。能机器化的规则不允许留在 `[judgment]` 里。

## 代码风格

`./gradlew spotlessApply` 一键格式化。CI 会跑 `spotlessCheck` 和 `detekt`。
