# designsystem — 模块契约

全项目唯一的视觉事实源：tokens、主题、动效、业务组件。UI 信条的浓缩版：
**用户感知的不是"漂亮"，而是连续性——没有瞬移、没有硬切、没有不理手指。**

## 依赖边界

- 不依赖任何 network / database / data / feature 模块。
- 只依赖 Compose runtime + material3 + graphics-shapes。

## 铁律

- [machine] 动效规格只许来自 `MaterialTheme.motionScheme` 或本模块 tokens；禁止 call-site 内联 `spring(`/`tween(`。 — detekt:MotionSpecInline
- [machine] 无 `GlobalScope`。 — detekt:NoGlobalScope
- [judgment] 新增组件必须同时在 `catalog` 注册一条 entry。 — reason: catalog 是活文档、截图基线和 designAudit 的数据源三合一；没有注册的组件等于没有验收。
- [judgment] 主题、动效、形状的扩展只允许走 token 覆盖，不允许在业务代码里硬编码字面值。 — reason: 换肤能力是本项目的卖点，字面量是它的天敌。

## 写组件前：先查复用表

原生组件自带动效规范、state layer、a11y、insets 处理；手写组件这些全丢。
**能说出原生组件为什么不适用，才允许手写。**

| 你想手写的 | 改用原生 | 可用性 |
|---|---|---|
| 一排悬浮图标按钮 | `HorizontalFloatingToolbar` / `VerticalFloatingToolbar` | material3 1.5.0-alpha |
| 分段操作按钮组 | `ButtonGroup` | 1.5.0-alpha |
| 按钮+下拉箭头 | `SplitButton` | 1.5.0-alpha |
| FAB 展开成操作列表 | `FloatingActionButtonMenu` | 1.5.0-alpha |
| 底部导航 | `ShortNavigationBar` | 1.4.0 稳定 |
| 平板侧边导航 | `WideNavigationRail` | 1.4.0 稳定 |
| 手机↔平板自适应导航 | `NavigationSuiteScaffold` | adaptive 1.2.0 |
| 加载指示器 | `LoadingIndicator`（形变多边形）/ `LinearWavyProgressIndicator` | 1.5.0-alpha |
| 横向滚动图片卡 | `HorizontalMultiBrowseCarousel` | 1.4.0 稳定 |
| 折叠标题栏 | `LargeFlexibleTopAppBar`（alpha）/ `LargeTopAppBar`（稳定） | 见注 |
| 下拉刷新 | `PullToRefreshBox` | 1.4.0 稳定 |
| 滑动删除列表项 | `SwipeToDismissBox` | 1.4.0 稳定 |
| 底部抽屉 | `ModalBottomSheet` — 永远不要手写 AnchoredDraggable | 1.4.0 稳定 |
| 吸附翻页 | `HorizontalPager` + `PagerDefaults.flingBehavior` | foundation |
| 列表项 | `ListItem` | 1.4.0 稳定 |
| 双栏主从布局 | `ListDetailPaneScaffold` | adaptive 1.2.0 |
| 共享元素转场 | Navigation 3 `sharedBounds` / `sharedElement` | nav3 稳定 |
| 加大圆角 | `ShapeDefaults.LargeIncreased` 等加大形状 | 1.5.0-alpha |

三个已知陷阱：
- `Card(onClick=…)` 没有 `indication` 参数（内部硬编码 ripple）。自定义按压反馈用非 clickable `Card` + `Modifier.pressableClickable { }`（见 `motion/PressFeedback.kt`）。
- `expressiveLightColorScheme()` 只有亮色，暗色配 `darkColorScheme()`。
- `FloatingToolbarDefaults.exitAlwaysScrollBehavior` 的 `exitDirection` 是必填参数。

版本真相以 `gradle/libs.versions.toml` 为准；升级 material3 时同步本表（playbook: bump-deps）。

## 动效约定（统一动效语言）

所有动效规格集中在 `motion/Motion.kt` 一处，call-site 禁止内联 `spring(`/`tween(`（detekt 把守）。
命名规格的语义就是选型依据——拿不准用哪个时，看名字：

| 规格 | 用途 |
|---|---|
| `Motion.softFloat/softDp/softInt/softOffset` | 通用值回落——安静、无过冲 |
| `Motion.snappyFloat` | 唯一允许弹跳（0.82 阻尼）的规格，用于"活泼"元素 |
| `Motion.contentSize` / `Motion.expandSize` | 内容驱动的尺寸变化 / 展开收起 |
| `Motion.indicator` | 分段控件指示器归位——快、无弹跳 |
| `Motion.paneRatio` | 分屏比例（split-view） |
| `Motion.press` | 按压反馈：缩到 0.96，无弹跳回弹 |
| `Motion.sheetAnchor` | 底部抽屉锚点归位——重、有物理感 |

**交互反馈**：`Modifier.pressableClickable { }` 一行拿到统一的按压缩放（`pressScale` + ripple）。
需要自定义 indication 时才用分离的 `pressScale(interactionSource)` + 自己的 `clickable`。

**转场编舞**：用 `motion/Transitions.kt` 的命名模式，不手写 enter/exit 对：
- `Transitions.fadeThrough()` — 屏幕切换默认（淡入淡出 + 0.98 缩放）
- `Transitions.sharedAxisY(forward)` — 层级导航（纵向）
- `Transitions.sharedAxisX(forward)` — 同级导航（横向：tab、翻页）
- `Transitions.listEnter()/listExit()` — 列表项增删（配合 `key` + `animateItem`）
- `Transitions.textSwap()` — 就地换值（计数器、标签）
- 缺模式先来这里加（带理由），再用。

**手势规则**：
- 拖动中 `snapTo`（1:1 跟手）；松手后才 animateTo。
- commit/cancel 按**速度**判定（"按动量决定，不按位置决定"）：快速轻扫在越过位置阈值前就提交远端锚点；阈值用速度归一化值（如 ratio/s），不用像素。
- 手势→动画交接传 `initialVelocity`。
- 速度阈值 + 位置阈值双保险（先看速度方向，速度不足时再看落点）。

**空间 vs effects**：spatial 规格允许过冲；effects（透明度/颜色）永不弹跳。
**帧预算**：动画变换走 `graphicsLayer`，不走 `offset(Dp)`/`padding`/`size`；每帧变化的状态用 lambda 读取。

## 验收

- [machine] 每个组件在 catalog 有 light + dark 两张截图基线，PR 不许无基线变更。 — ci:verifyRoborazziDebug
- [machine] 触控目标 ≥ 48dp、文本对比度 ≥ 4.5:1、每屏强调色 ≤ 3 族，由 designAudit 检查。 — task:designAudit
