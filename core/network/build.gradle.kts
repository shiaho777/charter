plugins {
    id("charter.android.library")
    alias(libs.plugins.kotlin.serialization)
    id("charter.android.hilt")
}

android {
    namespace = "dev.charter.core.network"
}

dependencies {
    api(libs.retrofit.core)
    api(libs.retrofit.kotlinx.serialization)
    api(libs.okhttp.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.okhttp.logging)
    implementation(libs.timber)

    implementation(project(":core:common"))
    implementation(project(":core:model"))

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
