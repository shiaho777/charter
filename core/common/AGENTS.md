# common — 模块契约

职责：纯 JVM 基础设施——`Result<T>`、`AppError` 错误分类、dispatcher 限定注解。

## 依赖边界

- 只依赖 kotlinx-coroutines 与 javax.inject；不得依赖 Android SDK、不得依赖其他 core 模块。

## 铁律

- [judgment] 可失败操作返回 `Result<T>`，异常在层边界转成 `AppError`。 — reason: 见 ADR-0002；错误类型穷举让 UI 状态机有穷举输入。
- [judgment] dispatcher 用限定注解（`@IoDispatcher` 等）注入，业务代码不裸写 `Dispatchers.X`。 — reason: 测试可替换调度器；裸写等于把测试的门焊死。
- [judgment] 本模块保持零 Android 依赖。 — reason: 纯 JVM 才能被任意模块（包括 tools 与测试）复用；引入 Android 依赖会把整个依赖图拖下水。
