# ADR 0007: 异步操作契约（SingleFlight / SerialQueue / SessionOwner / RateLimiter）

- 状态：Accepted
- 日期：2026-09-09

## 背景

UI 数据层的四个经典并发问题，每个都对应一类真实缺陷：

1. **重复请求**：双次下拉刷新、fragment 重建期间重复订阅 → 同一请求在飞两份。
2. **交错写**：快速连续操作（收藏/取消收藏）以不可预测的顺序落库。
3. **过期结果**：搜索输入提速后，慢的旧请求最后返回，覆盖了新结果（竞态陈旧）。
4. **洪泛**：用户狂点按钮，每个点击都触发一次 API 调用。

## 决定

`core:common/async/` 提供四个语义命名的工具：

| 工具 | 语义 | 何时用 |
|---|---|---|
| `SingleFlight` | 并发调用者加入在飞操作，共享命运（值或异常） | 用户触发的刷新、可重入的加载 |
| `SerialQueue` | 按提交顺序逐个执行，失败不阻塞队列 | 状态写入不得交错的序列 |
| `SessionOwner` | 版本化失效：新会话使旧会话过期，旧工作完成但结果作废 | "最新获胜"（搜索、筛选）无需取消异常 |
| `AsyncRateLimiter` | 操作间隔至少 N 毫秒 | API 礼貌、snackbar 节流 |

## 理由

- 这四个原语以 125 行覆盖了 UI 数据层绝大多数并发正确性问题。
- 语义命名 > 裸 Mutex：call-site 写 `refreshFlight.run { }` 而不是一把
  无名锁——读代码的人立刻知道并发契约是什么。
- `SessionOwner` 选择"完成但作废"而非取消：旧请求自然完成，没有
  CancellationException 传播的边界问题，只丢弃结果。

## 与现有 Flow 操作符的关系

- `debounce + flatMapLatest` 已覆盖"输入提速"场景（等价于 SessionOwner
  + 节流的组合）。ViewModel 的查询流继续用 Flow 操作符；SingleFlight 用在
  事件驱动的入口（refresh()）。
- Room 的 Flow 天然是"变更流"——单写者仓库 + 变更推送的
  模式我们免费获得，这是当初选 Room 做 SoT 的红利。

## 推翻条件

kotlinx.coroutines 未来提供等价的官方 API 时替换实现（语义保持）。
