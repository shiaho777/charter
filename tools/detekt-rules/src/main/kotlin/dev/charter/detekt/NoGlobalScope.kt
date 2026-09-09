package dev.charter.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

/**
 * `GlobalScope` leaks coroutines with no structured-concurrency parent.
 * Use viewModelScope / lifecycleScope / an injected @ApplicationScope.
 */
class NoGlobalScope(
    config: Config,
) : Rule(config) {
    override val issue =
        Issue(
            id = "NoGlobalScope",
            severity = Severity.Defect,
            description =
                "GlobalScope launches coroutines with no structured-concurrency " +
                    "parent; they leak past the screen that started them.",
            debt = Debt.TEN_MINS,
        )

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val ref = expression as? KtNameReferenceExpression ?: return
        if (ref.getReferencedName() == "GlobalScope") {
            report(
                CodeSmell(
                    issue,
                    Entity.from(ref),
                    "GlobalScope is forbidden; inject a scope (see core/common/dispatchers) " +
                        "or use viewModelScope/lifecycleScope",
                ),
            )
        }
    }
}
