# Playbook: 新增一个设计系统组件

1. **先查复用表**（`core/designsystem/AGENTS.md`）。原生组件能覆盖就不写；写不出来理由就回去用原生的。
2. **实现**：放 `core/designsystem/components/`。动效规格读 `MaterialTheme.motionScheme`；间距读 `LocalSpacing`；圆角读 `MaterialTheme.shapes`。硬编码字面值 = 打回。
3. **注册 catalog**：`catalog/registry/CatalogRegistry.kt` 加一条 `CatalogEntry`（名称 + 一句话说明 + content）。
4. **截图测试**：`catalog/src/test/.../CatalogScreenshotTest.kt` 加 light + dark 两个测试方法。
5. **生成基线**：`./gradlew :catalog:recordRoborazziDebug`。检查 `src/test/screenshots/` 新 PNG 确实正确——**亲眼打开看**。
6. **设计审计**：`./gradlew :catalog:designAudit` 通过（触控目标、对比度、强调色）。
7. **语义检查**：交互元素必须有 label（contentDescription 或可见文本），触控目标 ≥ 48dp。
8. **PR 必须包含**：组件代码 + catalog 注册 + 基线 PNG +（如有新约定）designsystem/AGENTS.md 更新。
