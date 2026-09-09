plugins {
    id("charter.jvm.library")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.collections.immutable)
    api(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
