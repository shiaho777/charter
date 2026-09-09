package dev.charter.architecture

import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Structural rules about how modules are allowed to see each other.
 * Violations here mean a dependency crossed a layer boundary.
 */
class ModuleBoundariesTest {
    @Test
    fun `designsystem has no network or database imports`() {
        charterFiles()
            .filter { it.path.contains("/core/designsystem/") }
            .assertTrue { file ->
                file.imports.none { import ->
                    import.name.startsWith("retrofit2") ||
                        import.name.startsWith("androidx.room") ||
                        import.name.startsWith("dev.charter.core.network") ||
                        import.name.startsWith("dev.charter.core.database")
                }
            }
    }

    @Test
    fun `features never touch network or database directly`() {
        charterFiles()
            .filter { it.path.contains("/feature/") }
            .assertTrue { file ->
                file.imports.none { import ->
                    import.name.startsWith("dev.charter.core.network") ||
                        import.name.startsWith("dev.charter.core.database") ||
                        import.name.startsWith("retrofit2") ||
                        import.name.startsWith("androidx.room")
                }
            }
    }

    @Test
    fun `features do not import other features`() {
        charterFiles()
            .filter { it.path.contains("/feature/") }
            .assertTrue { file ->
                val ownFeature = Regex("""/feature/([^/]+)/""").find(file.path)?.groupValues?.get(1)
                file.imports
                    .filter { it.name.startsWith("dev.charter.feature.") }
                    .none { import ->
                        val imported = import.name.removePrefix("dev.charter.feature.").substringBefore('.')
                        imported != ownFeature
                    }
            }
    }

    @Test
    fun `ViewModels are named ViewModels`() {
        charterFiles()
            .flatMap { it.classes() }
            .filter { cls -> cls.parents(indirectParents = true).any { it.name == "ViewModel" } }
            .assertTrue { it.name.endsWith("ViewModel") }
    }
}
