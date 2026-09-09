# build-logic

Convention plugins that encode Charter's build standards. New modules inherit
the full standard with a single plugin id — this is where "规范性" is enforced
at the build level.

| Plugin id | Purpose |
|---|---|
| `charter.android.application` | AGP app module + Kotlin Android + shared Android config |
| `charter.android.library` | AGP library module + Kotlin Android + shared Android config |
| `charter.android.compose` | Compose build features, BOM, compiler reports |
| `charter.android.feature` | library + compose + hilt + lifecycle + nav3 + designsystem |
| `charter.android.hilt` | KSP + Hilt |
| `charter.quality` | detekt (+ Charter rules) + spotless, root-level |
| `charter.jvm.library` | Pure JVM Kotlin library (core:common, core:model) |

Version pins for the build toolchain live in this module's `build.gradle.kts`;
runtime library versions live in `gradle/libs.versions.toml`. Bump both
together per `playbooks/bump-deps.md`.
