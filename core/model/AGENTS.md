# model — 模块契约

职责：领域模型（domain models），纯 JVM、零 Android 依赖、零框架依赖。

## 依赖边界

- 只依赖 kotlinx-serialization；不得依赖 network/database/data 的 DTO 或 Entity。
- 其他模块（network/database/data/feature）可以依赖本模块，本模块不依赖它们。

## 铁律

- [judgment] 模型是 data class，字段用领域语言命名，不带服务端/数据库的命名残留。 — reason: 领域层是应用的语言中心；让 JSON 的 snake_case 渗进领域模型，等于让线格式污染业务语言。
- [judgment] DTO/Entity 与 Model 的映射只发生在 data 层。 — reason: 三种类型各有主人，映射分散会把 schema 变更扩散到全依赖图。
