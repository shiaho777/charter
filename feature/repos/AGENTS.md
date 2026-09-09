# feature:repos — 模块契约

职责：GitHub 仓库浏览（搜索列表 + 详情）的完整示范 feature，是全项目的"标准答案"。

## 依赖边界

- 依赖 `core:designsystem` / `core:common` / `core:model` / `core:data`。
- 不依赖其他 feature、不依赖 network/database。

## 铁律

- [machine] ViewModel 暴露单一 `StateFlow<UiState>`；`*UiState` 是 sealed。 — konsist:HygieneTest
- [judgment] 每个屏幕四状态齐全：loading / empty / error / content+overflow。 — reason: 用户实际会遇到的状态必须在设计时就被回答，而不是上线后补洞。
- [judgment] 列表增删必须有 `key` + `Modifier.animateItem()`。 — reason: 没有稳定 key 的删除看起来是整列表上移——CRUD 应用里最刺耳的瞬间。
- [judgment] 路由是 `@Serializable` 类型，不是字符串。 — reason: 路由参数在编译期校验，字符串路由把错误推迟到运行时。
- [judgment] 事件是一次性的（导航、snackbar），用 SharedFlow/Channel，不进 UiState。 — reason: 状态是"现在是什么样"，事件是"刚刚发生了什么"，混为一谈会产生重复消费。

## 变更协议

新屏幕 → UiState + ViewModel + Screen + 四状态 + 截图测试 + 本文件更新（playbook: new-ui-screen）。
