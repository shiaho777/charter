# ADR 0006: 暂缓 AGP 9，首个版本基于 AGP 8.13

- 状态：Accepted
- 日期：2026-09-07

## 背景

AGP 9.x 引入 built-in Kotlin/Compose 模型（不再外挂 `org.jetbrains.kotlin.android`），convention plugin 的写法需要整体重写。同时新一代 androidx（compose 1.12、core-ktx 1.19、lifecycle 2.11、material3 1.5.0-alpha20+）强制要求 AGP ≥ 9.1。

## 决定

v1 基于 **AGP 8.13**，版本线锁定在 AGP-8 兼容区间（均经 AAR metadata 实测）：

| 依赖 | 版本 | AGP 要求 |
|---|---|---|
| material3 | 1.5.0-alpha15 | ≥ 8.6.0 |
| compose BOM | 2026.06.01（ui 1.11.4） | ≥ 8.6.0 |
| lifecycle | 2.10.0 | ≥ 8.6.0 |
| core-ktx | 1.18.0 | ≥ 8.9.1 |
| navigation3 | 1.1.7 | ≥ 8.9.1 |
| hilt-navigation-compose | 1.3.0 | ≥ 8.6.0 |
| okhttp | 5.4.0（compileSdk ≤ 36） | — |

AGP 9 迁移列入 v2 路线图：重写 convention plugins（built-in Kotlin/Compose）、Hilt → 2.60+、material3 → 最新 alpha、compileSdk → 37。

## 理由

- 一次性把"全新项目 + 全新构建模型"两个变量叠在一起，出了问题无法归因。
- AGP 8 线上每个版本都经过 AAR metadata 实证，确定性高于对新模型的推断。
- 升级路径本身就是本项目叙事的活教材：版本集中在 catalog，迁移由 Renovate + CI 守门。

## 推翻条件

material3 Expressive 所需 API 在 alpha15 上缺失且无法绕过时，立即启动 AGP 9 迁移。
