# ADR 0004: 纯 Android，不做 KMP 预留

- 状态：Accepted
- 日期：2026-09-07

## 背景

是否从第一天就兼容 Compose Multiplatform 决定 DI（Hilt vs Koin）与网络（Retrofit vs Ktor）的选型。

## 决定

**纯 Android**。用编译期校验最强的 Hilt + Retrofit 组合，先把一个平台做到极致规范。

## 理由

- 项目的第一卖点是"规范可机器化"，Hilt 的编译期依赖校验与之直接同构。
- KMP 兼容会迫使整套底层换成运行时解析的组件，规范性打点折扣。
- 多模块 + 清晰分层意味着未来若要 KMP，迁移成本可控——core:common/core:model 已是纯 JVM。

## 推翻条件

有真实 iOS/Desktop 需求时，重开 ADR 0001 评估 DI 迁移。
