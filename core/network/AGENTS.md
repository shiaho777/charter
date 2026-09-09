# network — 模块契约

职责：Retrofit/OkHttp 装配、DTO、API 接口、**错误边界**。

## 依赖边界

- 只依赖 `core:common`、`core:model`。feature 和 data 经由 `GitHubNetworkDataSource` 访问，不直接碰 Retrofit。
- DTO 不出本模块；对外只暴露 data source 返回的 `Result<T>`。

## 铁律

- [machine] 无 `GlobalScope`。 — detekt:NoGlobalScope
- [judgment] 所有 suspend 函数把可恢复异常转成 `Result.Failure(AppError)`，异常不许逃出本模块。 — reason: 错误类型穷举是 UI 状态机的地基；上层只处理五种 AppError，永远看不到 HttpException/IOException 本体。
- [judgment] DTO 加 `@Serializable` 并显式 `@SerialName`，字段全部带默认值。 — reason: 服务端加字段不该崩客户端；缺省值让 schema 演进向后兼容。
- [judgment] 不在本模块写业务逻辑（排序、过滤、聚合）。 — reason: 边界层只做协议翻译，业务规则属于 data/feature。

## 变更协议

新增端点 → 加 API 方法 + DTO + data source 方法（含错误映射）+ MockWebServer 单测。
