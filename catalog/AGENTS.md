# catalog — 模块契约

职责：设计系统的活文档 + 截图基线 + designAudit 数据源，三合一。

## 依赖边界

- 依赖 `core:designsystem`。可以展示 feature 组件，但不反向被依赖。

## 铁律

- [judgment] 设计系统的每个组件必须在此注册，否则视为未交付。 — reason: 注册即获得 showcase 页、明暗双截图基线、designAudit 检查；没有注册的组件在评审时不可见。
- [judgment] 截图基线（`src/test/screenshots/`）随 PR 一起提交，diff 进 code review。 — reason: 基线是视觉契约，变更必须在评审中被看见，而不是 CI 挂了才补。
- [machine] 语义导出（触控目标/文本）与截图配对产出。 — task:designAudit

## 变更协议

新组件 → 实现 + catalog entry + `recordRoborazziDebug` 生成基线 + designAudit 通过。
