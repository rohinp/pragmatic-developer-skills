# Project Profile Template

Use this template for the target repository's stateful project profile. Default path: `.codex/project-profile.md`.

The profile is working memory for future AI-agent sessions. Keep it short, factual, and useful for decisions.

## Header

```markdown
# Project Profile

Last updated: YYYY-MM-DD

Purpose: Durable engineering context for AI agents working in this repository.
```

## Recommended Sections

```markdown
## Project Context

- Domain:
- Product/runtime:
- Current maturity: Greenfield | Existing system | Legacy modernization | Maintenance
- Primary goals:

## Observed Conventions

- Architecture:
- Module boundaries:
- Naming and domain language:
- Error handling:
- Persistence/data access:
- Testing style:
- Tooling:

## User Decisions

| Date | Status | Decision | Rationale | Applies To |
| --- | --- | --- | --- | --- |
| YYYY-MM-DD | User decision |  |  |  |

## Provisional Choices

| Date | Choice | Why provisional | Revisit When |
| --- | --- | --- | --- |

## Rejected Alternatives

| Date | Alternative | Reason Rejected | Reconsider If |
| --- | --- | --- | --- |

## Domain Language

| Term | Meaning | Notes |
| --- | --- | --- |

## Quality Bar

- Testing expectations:
- Refactoring expectations:
- Performance expectations:
- Security/privacy expectations:
- Documentation expectations:

## Open Questions

| Date | Question | Impact |
| --- | --- | --- |
```

## Update Rules

- Add or update the profile when the user chooses between design options, accepts an architectural direction, names a domain concept, rejects an approach, or changes quality expectations.
- Keep observed conventions separate from user decisions. Observed conventions come from code; user decisions come from explicit user preference or approval.
- For existing repositories, fill `Observed Conventions` from the codebase before recommending major design changes.
- For greenfield projects, start with `Provisional Choices` and promote them to `User Decisions` only after the user confirms or repeated implementation makes them settled.
- When a decision changes, do not delete the old entry. Mark it `Superseded` and add the new decision.
- If profile guidance conflicts with current code, state the conflict before changing direction.
