plugins {
    id("charter.android.application")
    id("charter.android.compose")
    id("charter.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.charter.app"

    defaultConfig {
        applicationId = "dev.charter.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":feature:anime"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
}
