# data — 模块契约

职责：Repository 实现、DTO/Entity/Model 映射、数据层 DI 绑定。Room 是唯一事实源；网络是只写刷新通道。

## 依赖边界

- 依赖 `core:common` / `core:model` / `core:network` / `core:database`。
- feature 只允许依赖本模块的接口，不允许跨过去依赖 network/database（有架构测试把守）。

## 铁律

- [machine] feature 不直连 network/database。 — konsist:ModuleBoundariesTest
- [judgment] 读取永远返回 `Flow`，让 Room 推更新；写入=网络成功后 upsert 进 Room。 — reason: offline-first 的本质是"UI 只订阅本地"，网络失败时 UI 已有缓存可显示，错误状态降级为 snackbar 而非整屏。
- [judgment] dispatcher 注入（`@Dispatcher(CharterDispatchers.IO)`），不裸写 `Dispatchers.IO`。 — reason: 测试要替换调度器，裸写等于把测试的门焊死。
- [judgment] 映射函数写成扩展函数放 `mapper/`，DTO→Entity→Model 两段分开。 — reason: 三种类型各有主人（服务端 schema、本地 schema、领域语言），合并映射会把 schema 变更扩散到领域层。

## 变更协议

新增数据源 → 接口 + `Default*Repository` + `@Binds` 绑定 + 假实现放 `core:testing` + 单测。
