# ADR 0002: UDF + Result 类型，不用教条式 MVI，也不许裸异常

- 状态：Accepted
- 日期：2026-09-07

## 背景

状态管理与错误处理是 UI 架构的两条腿。候选：松散 MVVM、严格 MVI（Intent/Reducer）、UDF（单向数据流）。

## 决定

1. **UDF**：ViewModel 暴露单一不可变 `StateFlow<UiState>`；事件（导航/snackbar）走 SharedFlow/Channel 一次性消费。比松散 MVVM 规范，比 MVI 的 Intent/Reducer 样板务实。
2. **`Result<T, AppError>` 是全项目唯一合法的可失败返回类型**。异常在层边界（network/database）转换为五种穷举的 `AppError`（Network/Http/Serialization/Storage/Rejected/Unknown），上层永远不见原始异常。

## 理由

- 单一状态流让"四状态"（loading/empty/error/content）成为类型而非约定。
- 错误类型穷举让 UI 的状态机有穷举的输入，否则 error 分支只能靠猜。
- 裸异常跨层是"错误处理靠运气"的同义词。

## 推翻条件

某 feature 证明需要严格 MVI（如复杂的 undo/redo），可在该 feature 内局部采用，并在其模块 AGENTS.md 记录。
