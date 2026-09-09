plugins {
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // detekt's stable extension API — what custom rules compile against.
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.8")
    testImplementation(kotlin("test"))
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test-utils:1.23.8")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.8")
}

tasks.test {
    useJUnitPlatform()
}
