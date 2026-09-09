plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
