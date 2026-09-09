plugins {
    // Third-party plugins with `apply false`: their JARs land on the root
    // buildscript classpath (inherited by subprojects), which is what lets the
    // build-logic convention plugins apply them by id.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.roborazzi) apply false

    id("charter.quality")
    id("dev.charter.agent.contract")
}

agentContract {
    agentsFiles.from("AGENTS.md")
    agentsFiles.from(
        fileTree(rootDir) {
            include("*/AGENTS.md")
            include("*/*/AGENTS.md")
        },
    )
    ciWorkflows.from(".github/workflows")
}

tasks.register("verifyUi") {
    description = "One-command UI acceptance harness: builds, runs unit + screenshot " +
        "tests, audits design invariants, and verifies the agent contract."
    group = "verification"
    dependsOn("verifyAgentContract")
    dependsOn(":catalog:verifyRoborazziDebug", ":catalog:designAudit")
    dependsOn("check")
}
