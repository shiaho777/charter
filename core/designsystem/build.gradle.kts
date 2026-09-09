plugins {
    id("charter.android.library")
    id("charter.android.compose")
}

android {
    namespace = "dev.charter.core.designsystem"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
    }
}

dependencies {
    implementation(libs.compose.graphics.shapes)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.kotlinx.collections.immutable)
}
