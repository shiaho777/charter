pluginManagement {
    includeBuild("build-logic")
    includeBuild("tools")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Top-level includeBuild is required for dependency substitution (e.g.
// detektPlugins("dev.charter:detekt-rules")); pluginManagement.includeBuild
// only covers plugin resolution.
includeBuild("tools")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "charter"

include(":app")
include(":catalog")
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:testing")
include(":feature:repos")
include(":architecture-tests")
