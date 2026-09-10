# app — 模块契约

职责：参考应用壳。组装 feature 模块、承载 Application 类与导航宿主，自身不含业务逻辑。
当前形态：`:feature:anime` 的四 Tab 自适应外壳——compact 宽度用 `ShortNavigationBar`，
更宽用 `NavigationRail`（复用表指定，不手写底部导航）；Tab 共享单一 back stack，
详情/搜索/播放全屏压栈。

## 依赖边界

- 可依赖任何 core 模块与 feature 模块；任何模块不得依赖 app。
- 导航宿主（NavDisplay）在此组装各 feature 导出的 `entryProvider`。

## 铁律

- [machine] 无 `GlobalScope`。 — detekt:NoGlobalScope
- [judgment] app 壳保持"薄"：业务逻辑属于 feature/core，这里只有组装与生命周期接线。 — reason: 壳越薄，feature 越可复用、可测试；壳里长业务逻辑是模块化腐烂的第一个信号。
- [judgment] `enableEdgeToEdge()` 后用 WindowInsets 给内容留白，永不硬编码状态栏/导航栏高度。 — reason: targetSdk 35+ 强制边到边；硬编码栏高在不同设备上必然错位。
