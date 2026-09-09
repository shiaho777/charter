package dev.charter.designreview.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers `designAudit` on the module it is applied to. The module's tests
 * are expected to export SemanticsExport JSON files (screenshot + semantics)
 * into build/outputs/design-review before this task runs.
 */
class DesignReviewPlugin : Plugin<Project> {
    override fun apply(project: Project) =
        with(project) {
            val audit = tasks.register("designAudit", DesignAuditTask::class.java)

            audit.configure { task ->
                task.group = "verification"
                task.description = "Audits exported screens against Charter's design invariants " +
                    "(accent hues, touch targets, contrast)."
                task.inputDir.set(layout.buildDirectory.dir("outputs/design-review"))
                task.reportDir.set(layout.buildDirectory.dir("reports/design-review"))
                task.maxAccentHues.set(3)
                task.minTouchTargetDp.set(48f)
                task.minContrastRatio.set(4.5)
                task.failOnError.set(true)
                // Nothing exported yet (e.g. tests did not run) means nothing to audit.
                task.onlyIf {
                    task.inputDir
                        .get()
                        .asFile.isDirectory
                }
            }
        }
}
