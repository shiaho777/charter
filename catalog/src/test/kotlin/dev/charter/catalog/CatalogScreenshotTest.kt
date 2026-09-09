package dev.charter.catalog

import com.github.takahirom.roborazzi.captureRoboImage
import dev.charter.catalog.registry.catalogEntries
import dev.charter.core.designsystem.theme.CharterTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Golden screenshots for every registered catalog component, in both themes.
 *
 * Record:  ./gradlew :catalog:recordRoborazziDebug
 * Verify:  ./gradlew :catalog:verifyRoborazziDebug
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class CatalogScreenshotTest {
    @Test
    fun captureAllComponents() {
        catalogEntries.forEach { entry ->
            listOf(false to "light", true to "dark").forEach { (darkTheme, suffix) ->
                val png = File(screenshotsDir(), "${entry.name}_$suffix.png")
                captureRoboImage(filePath = png.absolutePath) {
                    CharterTheme(darkTheme = darkTheme, dynamicColor = false) {
                        entry.content()
                    }
                }
            }
        }
    }

    private fun screenshotsDir(): File =
        System.getProperty("roborazzi.output.dir")?.let(::File)
            ?: File("src/test/screenshots")
}
