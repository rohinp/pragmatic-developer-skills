# Spec Template

Use this template for system or feature specs in the target repository. Default directory: `.codex/specs/`.

Specs describe expected behavior. Project profiles describe durable engineering preferences. Keep them separate and cross-reference when useful.

## System Overview Placeholder

Create `.codex/specs/system-overview.md` during onboarding unless an equivalent spec already exists.

```markdown
# System Overview Spec

Last updated: YYYY-MM-DD

Status: Placeholder | Draft | Active

## Purpose

- What should the system do?
- Primary users:
- Primary outcomes:

## User Flows

| Flow | Actor | Trigger | Outcome | Status |
| --- | --- | --- | --- | --- |
| TBD | TBD | TBD | TBD | Open question |

## Domain Rules And Edge Cases

| Rule/Edge Case | Expected Behavior | Source | Status |
| --- | --- | --- | --- |
| TBD | TBD | TBD | Open question |

## API, Contract, And Data Behavior

| Surface | Contract/Data Behavior | Compatibility Notes | Status |
| --- | --- | --- | --- |
| TBD | TBD | TBD | Open question |

## Acceptance Criteria

- TBD

## Test Proof

| Behavior | Existing Test | Needed Test | Status |
| --- | --- | --- | --- |
| TBD | TBD | TBD | Open question |

## Open Questions

| Question | Impact | Owner/Source |
| --- | --- | --- |
| TBD | TBD | TBD |
```

## Feature Spec Template

Create feature specs as `.codex/specs/<feature-name>.md` when a non-trivial feature or change needs behavioral clarity.

```markdown
# <Feature Name> Spec

Last updated: YYYY-MM-DD

Status: Placeholder | Draft | Active | Superseded

## Goal

- What should this feature do?
- What problem does it solve?

## User Flows

1. TBD

## Acceptance Criteria

- [ ] TBD

## Domain Rules And Edge Cases

- TBD

## API, Contract, And Data Behavior

- Inputs:
- Outputs:
- Errors:
- Persistence/data changes:
- Backward compatibility:

## Test Proof

- Existing tests:
- New tests needed:
- Characterization tests needed:

## Dependencies And Constraints

- TBD

## Open Questions

- TBD
```

## Update Rules

- For existing projects, start with placeholders and fill from code, tests, docs, and user confirmation over time.
- Do not invent behavior to make the spec look complete.
- Mark uncertainty clearly with `TBD`, `Open question`, or `Assumption`.
- Update specs when acceptance criteria, domain rules, edge cases, contracts, data behavior, or test proof changes.
- Link specs to profile decisions when engineering preferences shape behavior, architecture, or testing.
