# Playbook: 新增一个 feature module

以 `feature:<name>` 为例。

1. **建模块**
   ```bash
   mkdir -p feature/<name>/src/main/kotlin/dev/charter/feature/<name>
   ```
2. **`feature/<name>/build.gradle.kts`**
   ```kotlin
   plugins {
       id("charter.android.feature")
       alias(libs.plugins.kotlin.serialization)
   }

   android {
       namespace = "dev.charter.feature.<name>"
   }
   ```
   convention plugin 已带上：library + compose + hilt + designsystem + lifecycle + navigation3 + immutable collections + core:testing 测试依赖。只需额外声明本 feature 特有依赖（如 `core:data`）。
3. **注册模块**：`settings.gradle.kts` 加 `include(":feature:<name>")`。
4. **导航契约**：`navigation/` 里定义 `@Serializable` 的 Key + `entryProvider` 导出函数（参考 `feature/repos/navigation/ReposNavigation.kt`）。
5. **写模块 AGENTS.md**：职责一句话 + 依赖边界 + 本模块特有约定。用 `feature/repos/AGENTS.md` 当模板。
6. **接入 app**：`app` 模块的 NavDisplay 注册该 feature 的 entries。
7. **架构测试**：确认 `:architecture-tests:test` 通过（边界规则自动生效，无需新增）。
8. **质量门禁**：`./gradlew detekt spotlessCheck testDebugUnitTest` 全绿再提 PR。

禁止事项：
- feature 之间互相依赖（共享逻辑下沉到 core）。
- feature 直接依赖 network/database（经由 core:data 接口）。
- 在 feature 里定义设计 token（那是 designsystem 的地盘）。
