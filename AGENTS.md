# AGENTS.md — Charter's constitution

You are working in **Charter**, an agent-native Android framework. This file is
the constitution. It is short on purpose: you are expected to read it fully,
every time.

Charter is three things wearing one trench coat:

1. **A reference app** (`:app` + `:feature:anime`) — a four-tab anime browser
   whose product prototype is borrowed from Kazumi (design only, no code; see
   `docs/adr/0008-kazumi-prototype.md`) — showing how to build a Compose
   application the Charter way. `:feature:repos` remains as the minimal
   single-feature exemplar.
2. **A design system** (`:core:designsystem` + `:catalog`) — Material 3
   Expressive on top of explicit tokens.
3. **A toolchain** (`:tools/*`) that makes the rules in this file real:
   - `tools/agent-contract` — verifies the claims in this file (`verifyAgentContract`)
   - `tools/design-review` — audits rendered screens (`:catalog:designAudit`)
   - `tools/detekt-rules` — static analysis for what lint can see (`detekt`)

## The one idea

A rule you cannot enforce is a suggestion. Charter therefore splits every rule
into exactly two kinds:

- **[machine]** — a check exists and runs. The check is named after the dash.
  `./gradlew verifyAgentContract` fails the build if a claimed check does not
  actually exist. Never let this file and the toolchain drift apart.
- **[judgment]** — no machine can decide this. The rule must carry its reason
  so you can apply it to cases the author never imagined.

If you add a rule, tag it. If you can automate it, automate it and tag it
`[machine]`. If not, tag it `[judgment]` and give the reason.

## Commands

```bash
./gradlew assembleDebug        # build the app
./gradlew testDebugUnitTest    # JVM tests (Robolectric where UI is involved)
./gradlew detekt               # static analysis incl. Charter's own rules
./gradlew spotlessCheck        # formatting
./gradlew :architecture-tests:test   # Konsist module-boundary tests
./gradlew :catalog:verifyRoborazziDebug   # screenshot regression
./gradlew :catalog:designAudit            # design invariants
./gradlew verifyAgentContract             # this file's claims, verified
./gradlew verifyUi                        # the whole acceptance harness
```

## Iron rules

- [machine] Motion specs come from `motion/Motion.kt` (the named token object) or `MaterialTheme.motionScheme` for stock components. No `spring(`/`tween(`/`keyframes(`/`snap(` at call sites. — detekt:MotionSpecInline
- [machine] No `GlobalScope`. Scopes are injected (`viewModelScope`, `lifecycleScope`, `@ApplicationScope`). — detekt:NoGlobalScope
- [machine] Module boundaries hold: features never import `core:network` or `core:database` directly; features never import other features; `:core:designsystem` never imports network/database code. — konsist:ModuleBoundariesTest
- [machine] No `android.util.Log` (Timber only); no raw `Dispatchers.X` outside dispatcher providers; `*UiState` types are sealed. — konsist:HygieneTest
- [machine] A one-command acceptance harness exists and stays wired. — task:verifyUi
- [machine] Design invariants are auditable on real renders. — task:designAudit
- [machine] This file's own claims are verified. — task:verifyAgentContract
- [machine] CI runs the contract verification on every change. — ci:verifyAgentContract
- [machine] CI runs static analysis. — ci:detekt
- [machine] CI runs formatting checks. — ci:spotlessCheck
- [machine] CI runs screenshot regression. — ci:verifyRoborazziDebug
- [machine] CI runs the acceptance harness. — ci:verifyUi

## Judgment rules (read the reasons — you will need them)

- [judgment] Gesture-driven values use `Animatable` + `VelocityTracker`, and every gesture→animation handoff passes `initialVelocity`. — reason: velocity discontinuity at finger-lift is the single biggest source of "this UI feels cheap"; no duration tuning fixes it, only carrying velocity does. Detect: flick hard and release; a visible pause or restart is a violation.
- [judgment] During a drag, values move with `snapTo` (1:1 with the finger); smoothing happens only after release. — reason: any animation between finger and element during a drag reads as lag.
- [judgment] Commit/cancel decisions use `calculateTargetValue` (where the object would land), not raw position. — reason: the UI should reason about momentum the way the hand does; a fast short flick should dismiss where a slow long drag should not.
- [judgment] Before writing any custom component, check the reuse table in `core/designsystem/AGENTS.md` and be able to say why the stock component does not fit. — reason: stock components ship the motion spec, state layers, a11y wiring and inset behaviour for free; hand-rolled ones silently drop all of it.
- [judgment] Every screen has four states — loading, empty, error, content-plus-overflow — and each is designed, not improvised. — reason: screens are reviewed in the states users actually hit, and overflow is where hand-rolled UI falls apart first.
- [judgment] One focus per screen. — reason: five things competing means the eye has nowhere to land; squint at the screenshot and if the answer isn't one thing, emphasis is wrong.
- [judgment] Every animation must answer "what does this help the user understand?" — reason: motion that answers nothing is noise; noise is what "over-designed" means.
- [judgment] Spatial specs may overshoot; effects specs (alpha, color) must never bounce. — reason: overshoot in space reads as alive; flickering opacity reads as a bug.
- [judgment] Nothing teleports: list mutations use stable `key` + `Modifier.animateItem()`; reflows use `animateBounds`; cross-screen pairs use `sharedBounds`/`sharedElement`. — reason: objects appearing or vanishing reads as cheap no matter how nice the easing is.
- [judgment] Animated transforms live in `graphicsLayer`, not layout (`offset(Dp)`, `padding`, `size`). — reason: composition and layout run per frame at 8.3 ms/frame; draw is the only phase with budget.
- [judgment] State that changes per frame is read inside lambdas (`graphicsLayer { }`, `derivedStateOf`), not passed as values. — reason: only the consuming node should invalidate; reading scroll state in composition recomposes the whole tree per pixel.
- [judgment] Critical actions pair an icon with a text label. — reason: unlabeled actions raised perceived modernity and tanked usability in the M3 Expressive research; familiarity is load-bearing.
- [judgment] Separate with space, not dividers. — reason: a divider between every row is a table, not a UI.
- [judgment] Back and dismiss gestures track the finger and stay cancellable mid-gesture. — reason: users change their minds mid-gesture, and the UI must visibly change its mind with them.
- [judgment] User-triggered refreshes use `SingleFlight` (`core:common/async`) so concurrent triggers join the in-flight request instead of duplicating it. — reason: double pull-to-refresh firing two identical requests is waste at best and a race at worst; the fix is a named primitive, not scattered booleans.
- [judgment] Sequential state writes that must not interleave use `SerialQueue`; latest-wins result gating uses `SessionOwner`. — reason: these are the two remaining classes of UI data races after Flow operators cover input debouncing; the primitives live in core:common with tests.
- [judgment] Error messages tell the user the next action, not the failure's internals ("请尝试更换来源" not "Failed to recognize file format"). — reason: the user cannot act on a stack trace; the only useful error is an actionable one. See docs/ui/product.md §2.
- [judgment] In-flight state is row-scoped, not page-scoped: each list item tracks its own operation, failures keep the row, and the UI auto-converges after operations complete. — reason: a full-page spinner for one row's delete erases 9 rows of context; see docs/ui/product.md §3.

## Repository map

| Path | What lives there |
|---|---|
| `app/` | The reference application shell (adaptive tab chrome + Nav3 host) |
| `catalog/` | Interactive design-system showcase + screenshot/audit tests |
| `core/designsystem` | Tokens, theme, motion, components — the only visual source of truth |
| `core/common` | Result/AppError, dispatcher qualifiers (pure JVM) |
| `core/model` | Domain models (pure JVM) |
| `core:network` | Retrofit wiring, DTOs, the error boundary |
| `core/database` | Room entities, DAOs |
| `core/data` | Repositories, mappers, DI bindings |
| `core/testing` | Test rules and fakes shared by all modules |
| `feature/anime` | The reference app's main feature (anime browser; prototype borrowed from Kazumi, ADR 0008) |
| `feature/repos` | The minimal feature exemplar (GitHub repo browser) |
| `architecture-tests/` | Konsist tests for module boundaries and hygiene |
| `build-logic/` | Convention plugins — where build standards live |
| `tools/` | agent-contract, design-review, detekt-rules |
| `playbooks/` | Step-by-step procedures for recurring work |
| `docs/adr/` | Architecture Decision Records — read before changing architecture |

## Where to go next

- Changing UI? Read `core/designsystem/AGENTS.md` and `docs/ui/taste.md`, then
  follow `playbooks/new-ui-screen.md`. A UI change is not done until the
  acceptance loop in that playbook has run.
- Changing architecture? Read the ADRs in `docs/adr/` first. To overturn one,
  write a new ADR.
- Adding a module? `playbooks/new-feature-module.md`.
- Upgrading dependencies? `playbooks/bump-deps.md`.
- Adding or changing a rule? `playbooks/add-agent-rule.md`.
