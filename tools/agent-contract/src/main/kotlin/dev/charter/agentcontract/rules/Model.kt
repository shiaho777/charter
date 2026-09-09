package dev.charter.agentcontract.rules

/** How a rule is kept honest. */
enum class RuleKind {
    /** The rule claims an automated check. `verifyAgentContract` must find it. */
    MACHINE,

    /** Human/agent judgment. Must carry a written rationale in the document. */
    JUDGMENT,
}

/** One enforcement target, e.g. `detekt:MotionSpecInline` or `task:testDebugUnitTest`. */
data class CheckRef(
    val type: String,
    val target: String,
) {
    override fun toString(): String = "$type:$target"
}

/** A single parsed rule from an AGENTS.md file. */
data class AgentRule(
    val kind: RuleKind,
    val checks: List<CheckRef>,
    val description: String,
    val file: String,
    val line: Int,
)

/** Result of verifying one rule. */
data class RuleVerification(
    val rule: AgentRule,
    val verifiedChecks: List<CheckRef>,
    val missingChecks: List<CheckRef>,
) {
    val ok: Boolean get() = missingChecks.isEmpty()
}

data class ContractReport(
    val rules: List<RuleVerification>,
) {
    val machineRules: List<RuleVerification> get() = rules.filter { it.rule.kind == RuleKind.MACHINE }
    val judgmentRules: List<RuleVerification> get() = rules.filter { it.rule.kind == RuleKind.JUDGMENT }
    val verified: Int get() = machineRules.count { it.ok }
    val total: Int get() = machineRules.size
    val percent: Int get() = if (total == 0) 100 else (verified * 100) / total
    val violations: List<RuleVerification> get() = machineRules.filterNot { it.ok }
}
