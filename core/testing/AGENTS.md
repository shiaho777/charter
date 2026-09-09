# testing — 模块契约

职责：全项目共享的测试基座——`MainDispatcherRule`、fake repository、测试数据工厂。

## 依赖边界

- 只被各模块的 `testImplementation` 依赖；不得被 main 源码集依赖。

## 铁律

- [judgment] ViewModel/Repository 测试用注入的 TestDispatcher + fake，不起 Hilt、不碰真网络/真数据库。 — reason: 单测要快、要确定；起 DI 容器或真实 IO 的"单测"其实是集成测试，慢且脆。
- [judgment] fake 实现放在这里而不是各模块私有。 — reason: 测试替身是跨模块的共享契约；各写一套会让 fake 与接口漂移。
