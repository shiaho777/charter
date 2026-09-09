plugins {
    id("charter.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.charter.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:data"))

    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.truth)
    api(libs.mockk)
}
