plugins {
    id("charter.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.charter.feature.anime"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit)
}
