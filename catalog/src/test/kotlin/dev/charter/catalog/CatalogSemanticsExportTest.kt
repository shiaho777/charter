package dev.charter.catalog

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import dev.charter.catalog.registry.catalogEntries
import dev.charter.core.designsystem.theme.CharterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Exports the semantics tree (clickability, text, bounds) for each catalog
 * component next to its screenshot, so `designAudit` can check touch-target
 * size and contrast against the rendered image.
 *
 * One @Test per entry: a compose rule's activity accepts setContent exactly
 * once, so each component gets its own test instance.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class CatalogSemanticsExportTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_light() = export("LoadingState", darkTheme = false)

    @Test
    fun loadingState_dark() = export("LoadingState", darkTheme = true)

    @Test
    fun errorState_light() = export("ErrorState", darkTheme = false)

    @Test
    fun errorState_dark() = export("ErrorState", darkTheme = true)

    @Test
    fun emptyState_light() = export("EmptyState", darkTheme = false)

    @Test
    fun emptyState_dark() = export("EmptyState", darkTheme = true)

    private fun export(
        entryName: String,
        darkTheme: Boolean,
    ) {
        val entry = catalogEntries.first { it.name == entryName }
        val suffix = if (darkTheme) "dark" else "light"
        composeTestRule.setContent {
            CharterTheme(darkTheme = darkTheme, dynamicColor = false) {
                entry.content()
            }
        }
        val density =
            ApplicationProvider
                .getApplicationContext<Context>()
                .resources.displayMetrics.density
        SemanticsExporter.export(
            provider = composeTestRule,
            screen = "${entry.name} ($suffix)",
            screenshotRelativePath =
                File(
                    screenshotsDir(),
                    "${entry.name}_$suffix.png",
                ).absolutePath,
            density = density,
            outFile = File(exportDir(), "${entry.name}_$suffix.json"),
        )
    }

    private fun screenshotsDir(): File =
        System.getProperty("roborazzi.output.dir")?.let(::File)
            ?: File("src/test/screenshots")

    private fun exportDir(): File = File("build/outputs/design-review")
}
