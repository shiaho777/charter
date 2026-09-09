plugins {
    // Loaded once here so subprojects do not load their own copies.
    kotlin("jvm") version "2.1.0" apply false
}

// Shared metadata for Charter tooling artifacts.
allprojects {
    group = "dev.charter"
    version = "0.1.0"
}
