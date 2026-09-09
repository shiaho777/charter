plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.0"
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        register("designReview") {
            id = "dev.charter.design.review"
            implementationClass = "dev.charter.designreview.gradle.DesignReviewPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
