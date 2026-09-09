# database — 模块契约

职责：Room 实体、DAO、数据库装配。

## 依赖边界

- 只依赖 `core:model`；对外只暴露 DAO 与 Entity，由 data 层消费。

## 铁律

- [judgment] DAO 查询返回 `Flow`，让 Room 把变更推给订阅者。 — reason: offline-first 的本质是"UI 只订阅本地事实源"；轮询接口会把缓存架构撕开。
- [judgment] Entity 带缓存元数据（如 `cachedAt`、来源 `query`），不混入领域概念。 — reason: 缓存失效策略依赖这些字段；领域语言属于 model，不属于存储。
- [judgment] schema 变更必须写 Migration，禁止依赖 `fallbackToDestructiveMigration` 上线。 — reason: 破坏性迁移清空用户数据；它是开发期便利，不是发布策略。
