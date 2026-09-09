plugins {
    id("charter.android.application")
    id("charter.android.compose")
    alias(libs.plugins.roborazzi)
    id("dev.charter.design.review")
}

android {
    namespace = "dev.charter.catalog"

    defaultConfig {
        applicationId = "dev.charter.catalog"
        versionCode = 1
        versionName = "0.1.0"
    }
}

roborazzi {
    // Golden images live in src/, so they are committed and diffable in PRs.
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.compose.ui.test.manifest)
}

// designAudit consumes what the screenshot tests export.
tasks.named("designAudit") {
    dependsOn("testDebugUnitTest")
}

// Screenshot tests spin up many Robolectric activities per JVM; the default
// 512m test heap OOMs the Recomposer thread.
tasks.withType<Test>().configureEach {
    maxHeapSize = "2g"
}
