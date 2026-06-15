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
- If the user provides a custom profile, import it as `Imported User Profile` first, then gradually normalize its durable preferences into `User Decisions`, `Provisional Choices`, `Rejected Alternatives`, `Domain Language`, and `Quality Bar`.
- When a decision changes, do not delete the old entry. Mark it `Superseded` and add the new decision.
- If profile guidance conflicts with current code, state the conflict before changing direction.

## Onboarding Script

Use `scripts/onboard_project.py` from this skill to initialize a target repository:

```bash
python3 scripts/onboard_project.py /path/to/project --agent both --mode existing
python3 scripts/onboard_project.py /path/to/project --agent codex --custom-profile /path/to/profile.md
python3 scripts/onboard_project.py /path/to/project --agent claude --mode greenfield
python3 scripts/onboard_project.py /path/to/project --agent claude --spec-dir .claude/specs
```

The script creates or updates `.codex/project-profile.md` and spec placeholders under `.codex/specs/` by default. It can also add:

- `AGENTS.md` for Codex guidance.
- `CLAUDE.md` for Claude guidance.

Use `--spec-dir .claude/specs` or another relative path when specs should live under a different agent folder.
Use `--no-specs` only when the target repository already has an equivalent spec workflow.

It does not copy the PDF resources into the target project.
