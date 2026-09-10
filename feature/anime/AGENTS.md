# feature:anime — 模块契约

职责：番剧浏览参考应用（推荐/时间表/搜索/详情/多源选择/收藏/历史/我的/播放占位），
是 Charter 参考 app 的主 feature。

**产品原型借鉴自开源项目 Kazumi（Predidit/Kazumi，GPL-3.0）**：仅借鉴其屏幕结构、
导航形态与交互模式（四 Tab 外壳、星期时间表、多源检索 Sheet、五态收藏、行级历史），
未复制任何源代码、美术资源或图标——GPL-3.0 与本项目 Apache-2.0 不兼容，代码级复制
被禁止。数据来自 Bangumi 开放 API。决策记录见 `docs/adr/0008-kazumi-prototype.md`。

## 依赖边界

- 依赖 `core:designsystem` / `core:common` / `core:model` / `core:data`。
- 不依赖其他 feature、不依赖 network/database。
- 播放源聚合（`PlaySourceAggregator`）是演示实现：不抓取任何第三方站点，
  播放页视频区域为占位 UI。

## 铁律

- [machine] ViewModel 暴露单一 `StateFlow<UiState>`；`*UiState` 是 sealed。 — konsist:HygieneTest
- [machine] 动效规格只来自 `motion/Motion.kt` tokens 或 `MaterialTheme.motionScheme`。 — detekt:MotionSpecInline
- [judgment] 每个屏幕四状态齐全：loading / empty / error / content+overflow。 — reason: 用户实际会遇到的状态必须在设计时就被回答，而不是上线后补洞。
- [judgment] 多源检索 Sheet 逐源独立状态与重试；单源失败不阻塞其他源。 — reason: 这是原型（Kazumi SourceSheet）的核心交互；整组失败重来会把一个源的超时放大成整屏不可用。
- [judgment] 收藏/历史写操作是行级的：每行跟踪自己的 in-flight 状态，失败保留该行。 — reason: docs/ui/product.md §3；全页 spinner 会抹掉其他行的上下文。
- [judgment] 列表增删必须有 `key` + `Modifier.animateItem()`。 — reason: 没有稳定 key 的删除看起来是整列表上移。
- [judgment] 路由是 `@Serializable` 类型，不是字符串。 — reason: 路由参数在编译期校验。
- [judgment] 错误文案面向下一步行动（"请检查网络后重试"），不暴露内部细节。 — reason: docs/ui/product.md §2。

## 变更协议

新屏幕 → UiState + ViewModel + Screen + 四状态 + VM 单测 + 截图 + 本文件更新（playbook: new-ui-screen）。
