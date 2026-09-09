# ADR 0003: Navigation 3，类型安全路由

- 状态：Accepted
- 日期：2026-09-07

## 背景

Compose 导航的两代方案：Navigation Compose 2.x（字符串路由）与 Navigation 3（`NavKey` 类型路由 + 声明式转场）。

## 决定

用 **Navigation 3**（`androidx.navigation3`）。路由是 `@Serializable` 类型，参数编译期校验；转场/共享元素/预测式返回是一等公民，与 Expressive 的动效叙事同代。

## 理由

- 字符串路由把参数错误推迟到运行时；类型路由在编译期拦下。
- `sharedBounds`/`sharedElement` 与 `predictivePopTransitionSpec` 是 Expressive 体验的核心机制，Nav3 原生内置，2.x 需要手工拼。
- feature 模块导出 `NavKey` + `entryProvider` 即完成导航契约，app 只负责组装。

## 代价

- Nav3 较新，API 仍在演进；通过 `gradle/libs.versions.toml` 单点 pin 住，升级走 bump-deps playbook。

## 推翻条件

官方宣布 Nav3 停止维护或核心 API 重大倒退。
