# ADR 0008: 参考应用的产品原型借鉴 Kazumi（仅设计，不抄代码）

- 状态：Accepted
- 日期：2026-09-09

## 背景

Charter 原参考应用只有一个 GitHub 仓库浏览器（`:feature:repos`），作为"Charter 方式
构建应用"的示范过于单薄：没有多目的地外壳、没有本地用户数据（收藏/历史）、没有
sheet/分页/多源聚合这类真实产品里的交互密度。

[Kazumi](https://github.com/Predidit/Kazumi)（GPL-3.0，Flutter）是一个成熟的番剧采集
与在线观看应用，其产品结构恰好覆盖上述所有空白：四 Tab 外壳、星期时间表、多源检索
Sheet、五态收藏、行级观看历史。

## 决定

1. **`:feature:anime` 的产品原型借鉴 Kazumi**：屏幕结构、导航形态、交互模式与功能
   设计以 Kazumi 为蓝本（借鉴点清单见下）。
2. **只借鉴设计，绝不复制实现**。Kazumi 是 GPL-3.0，Charter 是 Apache-2.0——GPL 代码
   不能进入 Apache 项目。因此：不复制任何 Dart 源码、不复制任何美术资源/图标/截图、
   不引入其插件规则文件。所有界面基于 Charter 设计系统（`core:designsystem`）用
   Jetpack Compose 重新实现，所有数据层遵循 Charter 自己的 core 分层模式。
3. **不移植视频源抓取**。Kazumi 的真实播放依赖 XPath 插件抓取第三方站点，存在法律
   灰区且工程量巨大。Charter 用 `PlaySourceAggregator` 的**演示实现**替代：保留
   "逐源并发检索、独立状态、独立重试、可改关键词"的招牌交互，但源是内置的虚构
   演示源，播放页是占位 UI，不联网抓取、不播放真实视频流。
4. **元数据走 Bangumi 开放 API**（api.bgm.tv，与 Kazumi 同源）：推荐/时间表/搜索/
   详情/剧集，Room 离线缓存，`Result` 错误边界——完全复用 GitHub 链路确立的模式。
5. **归属声明**：README、`feature/anime/AGENTS.md` 与应用内"关于"页均注明原型出处
   与借鉴边界。

## 借鉴点清单（设计层面）

| Kazumi 原型 | Charter 实现 |
|---|---|
| 竖屏底部导航 / 横屏侧边 Rail 四 Tab（推荐/时间表/追番/我的） | `AppRoot` 按 `WindowSizeClass` 切换 `ShortNavigationBar` / `NavigationRail` |
| 星期胶囊时间表（当日高亮、每日数量、只看追番） | `ScrollableTabRow` 七日 Tab + 追番过滤 |
| 番剧目录网格 + 分页 | `LazyVerticalGrid` 海报卡（列数随窗口 3/5/6）+ 滚动分页 |
| 详情页多源检索 SourceSheet（逐源状态/重试/改关键词/线路+集数） | `SourceSheet` + `SourceSheetViewModel`（演示源，交互语义一致） |
| 播放页选集面板（线路切换、分段集数网格、已看进度标记） | `PlayerScreen`（占位播放器 + 完整选集面板） |
| 五态收藏（在看/想看/搁置/看过/抛弃） | `CollectStatus` + 收藏页分类 chips + 长按改类 |
| 行级观看历史（续播、管理模式、清空确认） | 历史页（进度条、批量选择、确认对话框） |

## 与原型的主要偏差（均有原因）

- **推荐页 = 当季新番**：Bangumi 的 `/v0/trending` 与 `/p1/trending` 公共端点已失效
  （实测 404，Kazumi 依赖其自建镜像）。改用官方支持的搜索端点 + `air_date` 季度
  过滤，数据真实且不依赖第三方镜像。
- **季度选择弹窗挂在推荐页**（原型挂在时间表）：季度本质是"另一个 air_date 窗口"，
  与推荐页的当季目录同构；时间表保持"本周放送"的单一职责。
- **无插件系统、无下载、无云同步、无弹幕/超分**：超出参考应用需要的演示范围，且
  插件抓取是法律灰区（见决定 3）。"我的"页保留这些入口的禁用态并如实标注。

## 代价与对策

- GPL 边界靠自律而非机器：`feature/anime/AGENTS.md` 与本 ADR 写明禁令；代码评审时
  对照 Kazumi 仓库任何"逐行相似"都是红旗。
- 演示播放源可能被误解为真实功能：关于页、SourceSheet 文案、播放页占位区三处
  都明确标注"演示"。
- Bangumi API 无限流承诺：UA 头标识应用、Room 缓存兜底、`Rejected(429)` 有专属
  用户文案。

## 推翻条件

若需要真实播放能力，应另立 ADR 评估合规的视频源（如自有内容或授权 CDN），而不是
恢复对第三方站点的规则抓取。
