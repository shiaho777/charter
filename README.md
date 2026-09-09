# Charter

**An agent-native Android framework: a reference app, a design system, and a
toolchain that makes engineering standards enforceable.**

Charter is three things wearing one trench coat:

1. **A reference app** (`:app` + `:feature:repos`) showing how to build a
   Compose application the Charter way.
2. **A design system** (`:core:designsystem` + `:catalog`) — Material 3
   Expressive on top of explicit tokens.
3. **A toolchain** (`tools/*`) that makes the rules real:
   - `tools/agent-contract` — verifies the claims in AGENTS.md
     (`./gradlew verifyAgentContract`)
   - `tools/design-review` — audits rendered screens against design invariants
     (`./gradlew :catalog:designAudit`)
   - `tools/detekt-rules` — static analysis for what lint can see (`detekt`)

![agent rules: machine-enforced](https://img.shields.io/badge/agent_rules-machine--enforced-brightgreen)

> **Why "agent-native"?** Charter is built for development by AI coding agents
> (and humans who work with them). Every rule in `AGENTS.md` is either
> machine-enforced (with the enforcing check named in-line) or carries an
> explicit written rationale so an agent can generalize it correctly. The
> toolchain fails the build when docs and enforcement drift apart.

## Quick start

```bash
git clone <repo> && cd charter

# Build
./gradlew assembleDebug

# The full acceptance harness — one command:
./gradlew verifyUi
```

`verifyUi` aggregates: `verifyAgentContract` → unit tests → Konsist
architecture tests → Roborazzi screenshot regression → `designAudit` →
`detekt` → `spotlessCheck`.

## Repository map

| Path | What lives there |
|---|---|
| `app/` | Reference application shell (nav host, DI root) |
| `catalog/` | Interactive design-system showcase + screenshot/audit tests |
| `core/designsystem` | Tokens, theme, motion, components — the only visual source of truth |
| `core/common` | `Result`/`AppError`, dispatcher qualifiers (pure JVM) |
| `core/model` | Domain models (pure JVM) |
| `core/network` | Retrofit wiring, DTOs, the error boundary |
| `core/database` | Room entities, DAOs |
| `core/data` | Repositories, mappers, DI bindings |
| `core/testing` | Test rules and fakes shared by all modules |
| `feature/repos` | The reference feature (GitHub repo browser) |
| `architecture-tests/` | Konsist tests for module boundaries and hygiene |
| `build-logic/` | Convention plugins |
| `tools/` | agent-contract, design-review, detekt-rules |
| `playbooks/` | Step-by-step procedures for recurring work |
| `docs/adr/` | Architecture Decision Records |

## Commands

```bash
./gradlew assembleDebug              # build the app
./gradlew testDebugUnitTest          # JVM tests (Robolectric where UI is involved)
./gradlew detekt                     # static analysis incl. Charter's own rules
./gradlew spotlessCheck              # formatting
./gradlew :architecture-tests:test   # Konsist module-boundary tests
./gradlew :catalog:verifyRoborazziDebug   # screenshot regression
./gradlew :catalog:designAudit            # design invariants
./gradlew verifyAgentContract             # this file's claims, verified
./gradlew verifyUi                        # the whole acceptance harness
```

## The rules

See [AGENTS.md](AGENTS.md). Every rule there is either machine-enforced (the
enforcing check is named in-line) or carries a written rationale so an agent —
or a human — can apply it to cases the author never imagined.

## License

Apache License 2.0. See [LICENSE](LICENSE).
