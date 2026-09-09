package dev.charter.agentcontract.rules

import java.io.File

/**
 * Parses AGENTS.md files into [AgentRule]s.
 *
 * Recognised bullets (anywhere in the file):
 *   - [machine] Some rule text — detekt:MotionSpecInline
 *   - [machine] Some rule — detekt:X, task:testDebugUnitTest
 *   - [judgment] Some rule — reason: why this exists
 *
 * Kind names `机器强制` / `判断` are accepted as aliases, so documents can be
 * written in Chinese without losing machine-checkability.
 */
object AgentRulesParser {
    private val bullet = Regex("""^\s*[-*]\s+(.*)$""")

    private val kindMachine = setOf("machine", "机器强制")
    private val kindJudgment = setOf("judgment", "判断")

    private val checkRef = Regex("""^(detekt|konsist|task|ci):([\w.\-/:]+)""")

    fun parse(file: File): List<AgentRule> {
        if (!file.isFile) return emptyList()
        val rules = mutableListOf<AgentRule>()
        file.readLines().forEachIndexed { index, raw ->
            val text =
                bullet
                    .matchEntire(raw)
                    ?.groupValues
                    ?.get(1)
                    ?.trim() ?: return@forEachIndexed
            parseBullet(text, file.path, index + 1)?.let(rules::add)
        }
        return rules
    }

    private fun parseBullet(
        text: String,
        path: String,
        line: Int,
    ): AgentRule? {
        if (!text.startsWith("[")) return null
        val close = text.indexOf(']')
        if (close < 0) return null
        val kind =
            when (text.substring(1, close).trim()) {
                in kindMachine -> RuleKind.MACHINE
                in kindJudgment -> RuleKind.JUDGMENT
                else -> return null
            }

        val body = text.substring(close + 1).trim()
        val (description, refsPart) = splitOnDash(body)
        val checks =
            refsPart
                .split(Regex("""[，,]"""))
                .map { it.trim() }
                .mapNotNull { raw ->
                    checkRef.matchEntire(raw)?.let { m ->
                        CheckRef(m.groupValues[1], m.groupValues[2])
                    }
                }

        return AgentRule(
            kind = kind,
            checks = checks,
            description = description.ifBlank { body },
            file = path,
            line = line,
        )
    }

    /** Split "description — refs" on an em/en dash; refs may be absent. */
    private fun splitOnDash(body: String): Pair<String, String> {
        for (dash in listOf(" — ", " – ")) {
            val i = body.lastIndexOf(dash)
            if (i >= 0) {
                val left = body.substring(0, i).trim()
                val right = body.substring(i + dash.length).trim()
                if (right.isNotEmpty() && (right.contains(':') || isJudgmentReason(right))) {
                    return left to right
                }
            }
        }
        return body to ""
    }

    private fun isJudgmentReason(text: String): Boolean =
        text.startsWith("reason:", ignoreCase = true) || text.startsWith("理由：")
}
