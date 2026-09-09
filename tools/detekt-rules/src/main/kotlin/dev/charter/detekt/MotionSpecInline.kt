package dev.charter.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

/**
 * Motion specs must come from the theme (MaterialTheme.motionScheme) or the
 * central motion token file — never inline at a call site.
 *
 * Distilled from ui_ux_engineering Step 3's grep test:
 *   every `spring(`/`tween(` outside the motion token file is motion drift.
 *
 * Excluded paths: designsystem tokens, theme, and anything under src/test.
 */
class MotionSpecInline(
    config: Config,
) : Rule(config) {
    override val issue =
        Issue(
            id = "MotionSpecInline",
            severity = Severity.Style,
            description =
                "Inline animation spec at a call site. Motion belongs to " +
                    "MaterialTheme.motionScheme or core/designsystem/tokens/Motion.kt so " +
                    "the whole app shares one choreography.",
            debt = Debt.FIVE_MINS,
        )

    private val specFactories = setOf("spring", "tween", "keyframes", "snap", "repeatable", "infiniteRepeatable")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = (expression.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
        if (callee !in specFactories) return

        val path = expression.containingKtFile.virtualFilePath
        if (isAllowed(path)) return

        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "'$callee(...)' inline at call site; read the spec from " +
                    "MaterialTheme.motionScheme or Motion tokens instead",
            ),
        )
    }

    private fun isAllowed(path: String): Boolean =
        path.contains("/tokens/") ||
            path.contains("/theme/") ||
            path.contains("/src/test/") ||
            path.contains("/src/androidTest/") ||
            path.contains("designsystem")
}
