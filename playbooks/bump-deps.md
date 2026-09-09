# Playbook: 升级依赖（bump-deps）

版本只许出现在 `gradle/libs.versions.toml` 一处。任何文档/AGENTS.md 里的版本号都是引用说明，不是事实源。

## 常规升级

1. 改 `gradle/libs.versions.toml` 里的版本（pin 精确版本，禁用区间/动态版本）。
2. `./gradlew build verifyAgentContract` 全绿。
3. 若升级的是 **material3**（尤其 alpha 线）：
   - 读对应版本的 release notes，API 面在 alpha 之间会动（`MotionScheme`、`MaterialShapes` 都改过名）。
   - 同步更新 `core/designsystem/AGENTS.md` 复用表里的"可用性"列。
   - 跑 `./gradlew :catalog:recordRoborazziDebug` 重新生成基线，diff 逐张看——动效/形状变化必须被亲眼确认。
4. 若升级的是 **AGP/Kotlin**：同时检查 `build-logic` 里的 compileOnly 依赖版本与 catalog 一致。
5. PR 描述写清：升了什么、为什么、回归验证方式。

## 从不做的事

- 不在模块 build 文件里写版本号（convention plugin 和 catalog 之外出现版本号即违规）。
- 不静默降级：某个依赖升不上去时，在 PR 里写明原因，而不是偷偷回退。
- 不让 CI 替你发现回归：本地先跑 `./gradlew verifyUi`。
