plugins {
    id("charter.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.charter.feature.repos"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    testImplementation(libs.junit)
}
