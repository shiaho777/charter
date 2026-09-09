package dev.charter.architecture

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Hygiene rules that keep the codebase boring — boring is the goal. */
class HygieneTest {
    @Test
    fun `no android util Log, Timber only`() {
        charterFiles()
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("android.util.Log") }
            }
    }

    @Test
    fun `no Dispatchers dot references outside dispatcher providers and testing`() {
        charterFiles()
            .filter { file ->
                file.path.contains("/src/main/") &&
                    !file.path.contains("/core/common/") &&
                    !file.path.contains("/core/data/") &&
                    !file.path.contains("/core/testing/")
            }.assertFalse { file ->
                Regex("""Dispatchers\.(IO|Main|Default)""").containsMatchIn(file.text)
            }
    }

    @Test
    fun `ui state types are sealed`() {
        val uiStateTypes =
            charterFiles()
                .flatMap { it.classes() + it.interfaces() }
                .filter { it.name.endsWith("UiState") }
        assertThat(uiStateTypes.isNotEmpty()).isTrue()
        assertWithMessage("All *UiState types must be sealed")
            .that(uiStateTypes.all { it.hasSealedModifier })
            .isTrue()
    }

    @Test
    fun `screens expose the four states`() {
        charterFiles()
            .filter { it.path.contains("/feature/") && it.name.endsWith("UiState.kt") }
            .assertTrue { file ->
                val text = file.text
                listOf("Loading", "Error").all { state -> text.contains(state) }
            }
    }
}
