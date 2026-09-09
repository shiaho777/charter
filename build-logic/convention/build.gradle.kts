import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.charter.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.13.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.0")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.0-1.0.29")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    compileOnly("com.diffplug.spotless:spotless-plugin-gradle:8.10.2")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "charter.android.application"
            implementationClass = "dev.charter.gradle.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "charter.android.library"
            implementationClass = "dev.charter.gradle.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "charter.android.compose"
            implementationClass = "dev.charter.gradle.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "charter.android.feature"
            implementationClass = "dev.charter.gradle.AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "charter.android.hilt"
            implementationClass = "dev.charter.gradle.HiltConventionPlugin"
        }
        register("quality") {
            id = "charter.quality"
            implementationClass = "dev.charter.gradle.QualityConventionPlugin"
        }
        register("jvmLibrary") {
            id = "charter.jvm.library"
            implementationClass = "dev.charter.gradle.JvmLibraryConventionPlugin"
        }
    }
}
