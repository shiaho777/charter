package dev.charter.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetId
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CharterRuleSetProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = "charter"

    override fun instance(config: Config): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(
                MotionSpecInline(config),
                NoGlobalScope(config),
            ),
        )
}
