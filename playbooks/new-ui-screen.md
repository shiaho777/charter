# Playbook: 新增一个 UI 屏幕

产出物清单（缺一不可）：

1. **UiState** — sealed interface，含 Loading / Empty / Error / Content 四状态（Content 要覆盖溢出场景）。
2. **ViewModel** — 单一 `StateFlow<UiState>`；事件走 SharedFlow；dispatcher 注入。
3. **Screen** — 四状态各有 UI；组件优先复用（先查 `core/designsystem/AGENTS.md` 复用表）。
4. **单测** — ViewModel 状态机测试（用 `core:testing` 的 MainDispatcherRule + fake repository）。
5. **截图** — 关键状态在 catalog 或 feature 测试里有明暗双截图。
6. **文档** — 所属模块的 AGENTS.md 若引入新约定，同步更新。

## 验收循环（不许跳过）

一个你看都没看过的 UI 不算完成。按顺序执行，至少迭代一轮：

1. 编译：`./gradlew :feature:<name>:assembleDebug`。编译不过就修，不许删功能绕过。
2. 跑起来：在模拟器/设备上安装运行，亲眼截图查看。
3. 四状态逐一出图：loading / empty / error / 溢出（最长真实文本、最大系统字号）。
4. 明暗双主题各出一张。
5. 动效用真实手势验：快速甩动列表、滑动删除、返回手势——动画起点必须接手势速度（参考根 AGENTS.md 判断规则）。
6. 跑 `./gradlew :catalog:designAudit`（或 feature 对应测试）检查触控目标/对比度/强调色。
7. logcat 里不能有 `Skipped N frames` / `Davey!` 警告。
8. 修掉看到的问题，回到第 2 步。第一轮渲染几乎从来不该是交付版本。

没有设备/模拟器时：明说，并用 Roborazzi 截图 + `@Preview` 代替，同时在 PR 描述里标注"未实机验证"。**静默跳过等于撒谎。**

## 验收门槛（客观标准，不是自我感觉）

| 项 | 门槛 | 检查方式 |
|---|---|---|
| 编译 | 零 error | 构建输出 |
| 触控目标 | ≥ 48dp | designAudit |
| 文本对比度 | ≥ 4.5:1 | designAudit |
| 强调色 | ≤ 3 族/屏 | designAudit |
| 帧跳过 | logcat 无 Skipped 警告 | 实机/模拟器滚动 |
| 四状态 | 各有一张截图 | PR 附件或截图基线 |
| 暗色模式 | 看过，不只是切过 | 截图 |
