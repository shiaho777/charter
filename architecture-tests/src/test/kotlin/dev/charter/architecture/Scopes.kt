package dev.charter.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/**
 * Module source sets under test. scopeFromModule resolves against the project
 * root (found via settings.gradle.kts) and reads only the module's sources —
 * never build outputs, generated code, or .gradle caches.
 */
private val charterModules =
    listOf(
        "app",
        "catalog",
        "core/common",
        "core/model",
        "core/designsystem",
        "core/network",
        "core/database",
        "core/data",
        "core/testing",
        "feature/repos",
        "feature/anime",
    )

fun charterFiles(): List<KoFileDeclaration> =
    charterModules.flatMap { module ->
        runCatching { Konsist.scopeFromModule(module).files }.getOrElse { emptyList() }
    }
