plugins {
    id("charter.android.library")
    alias(libs.plugins.kotlin.serialization)
    id("charter.android.hilt")
}

android {
    namespace = "dev.charter.core.database"
}

dependencies {
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(project(":core:model"))

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
