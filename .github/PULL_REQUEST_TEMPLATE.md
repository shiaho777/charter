## What & why

<!-- One paragraph. What changed, why it was needed. -->

## Evidence

<!-- Required. Paste the output of the acceptance harness. -->

- [ ] `./gradlew verifyUi` passed locally
- [ ] `verifyAgentContract` report attached (if AGENTS.md changed)
- [ ] `designAudit` report attached (if UI changed)

## Screenshots

<!-- UI changes: light + dark, all four states (loading/empty/error/overflow). -->

| State | Light | Dark |
|---|---|---|
| Content | | |
| Loading | | |
| Empty | | |
| Error / overflow | | |

## Checklist

- [ ] Read the module's `AGENTS.md`; this change follows it
- [ ] No new `[judgment]` rule that could be a `[machine]` rule
- [ ] New component → registered in catalog + screenshot baselines committed
- [ ] `spotlessCheck` + `detekt` green
