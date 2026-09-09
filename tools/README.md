# Charter Tools

Charter's proprietary toolchain — three self-developed artifacts that enforce
the project's engineering standards.

| Module | Purpose | Key Task / Output |
|---|---|---|
| `agent-contract` | Verifies AGENTS.md `[machine]` rules have real checks | `verifyAgentContract` + badge JSON |
| `design-review` | Audits rendered screens against design invariants | `designAudit` + report JSON |
| `detekt-rules` | Custom detekt rules (`MotionSpecInline`, `NoGlobalScope`) | Loaded via `detektPlugins` |

Each module builds as part of the composite build (`includeBuild("tools")` in
`settings.gradle.kts`). The Gradle plugins (`agent-contract`, `design-review`)
are applied to the main build by plugin id; the detekt rules jar is consumed
via dependency substitution (`detektPlugins("dev.charter:detekt-rules")`).
