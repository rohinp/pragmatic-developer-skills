# Pragmatic Developer Skill

This repository contains the `pragmatic-developer` skill. The skill guides an AI coding agent to work like a pragmatic senior developer: align with the existing codebase, respect domain modeling, use tests as behavioral proof, refactor in small safe steps, and record durable project decisions.


## Repository Layout

```text
.
├── SKILL.md
├── agents/
│   └── openai.yaml
├── references/
│   ├── pragmatic-engineering.md
│   ├── project-profile.md
│   ├── review-checklists.md
│   ├── source-map.md
│   └── spec-template.md
└── scripts/
    └── onboard_project.py
```

## What The Skill Provides

- Existing repo alignment before design changes.
- Greenfield preference building over time.
- Stateful project profile support at `.codex/project-profile.md` by default.
- Spec placeholders at `.codex/specs/` by default.
- Optional Codex and Claude guidance files in target projects.
- Custom user profile import into the project profile.
- Engineering guidance for DDD, SOLID, YAGNI, KISS, TDD, refactoring, clean code, design patterns, algorithms, and code review.

## Onboard A Target Project

From this repository:

```bash
  ./scripts/onboard_project.py /path/to/project --agent both --mode existing
```

This creates or updates:

```text
/path/to/project/.codex/project-profile.md
/path/to/project/.codex/specs/system-overview.md
/path/to/project/.codex/specs/_feature-template.md
/path/to/project/AGENTS.md
/path/to/project/CLAUDE.md
```

Use Codex only:

```bash
./scripts/onboard_project.py /path/to/project --agent codex --mode existing
```

Use Claude only and keep specs under `.claude/specs`:

```bash
./scripts/onboard_project.py /path/to/project --agent claude --mode existing --spec-dir .claude/specs
```

For a greenfield project:

```bash
./scripts/onboard_project.py /path/to/project --agent both --mode greenfield
```

Import an existing user profile or architecture note:

```bash
./scripts/onboard_project.py /path/to/project --custom-profile /path/to/profile.md
```

Skip spec placeholders only when the target project already has an equivalent spec workflow:

```bash
./scripts/onboard_project.py /path/to/project --no-specs
```

## Existing Project Spec Behavior

For existing projects, specs are built gradually. The agent should not pretend it fully understands the system after a scan.

What onboarding does automatically:

- Creates `.codex/specs/system-overview.md`.
- Creates `.codex/specs/_feature-template.md`.
- Adds Codex/Claude guidance telling agents to read and update specs.

What an agent may do while scanning an existing project:

- Add observed behavior to specs when it is supported by code, tests, API schemas, docs, or configuration.
- Record existing user flows only when they can be traced to evidence.
- Record domain rules and edge cases found in tests, validation logic, business logic, schemas, or docs.
- Record API, contract, and data behavior when it is visible in code or documentation.
- Mark uncertain behavior as `TBD`, `Open question`, or `Assumption`.

What an agent must not do:

- Do not invent full specs from guesses.
- Do not convert weak assumptions into accepted behavior.
- Do not remove open questions just to make a spec look complete.
- Do not override existing repo conventions with generic spec-driven process.

What is optional for the user or developer:

- The user may manually fill the placeholders.
- The user may provide a custom profile or architecture note with `--custom-profile`.
- The agent may gradually normalize user-provided notes into project profile decisions and specs.
- A future script mode may perform an evidence-based scan, but it should still leave unknowns explicit.

The intended flow for an existing project is:

```text
1. Run onboarding.
2. Agent scans code, tests, docs, and contracts.
3. Agent fills only evidence-backed spec details.
4. Agent leaves unknowns as TBD or open questions.
5. User confirms or corrects behavior over time.
6. Feature work creates or updates feature specs.
7. Tests prove the behavior described in specs.
```

## Development Instructions

Edit `SKILL.md` for behavior that must always be loaded when the skill triggers.

Put deeper guidance in `references/`:

- `pragmatic-engineering.md`: engineering heuristics.
- `project-profile.md`: stateful project profile template and update rules.
- `spec-template.md`: system and feature spec templates.
- `review-checklists.md`: review and implementation prompts.
- `source-map.md`: source material map.

Put deterministic setup or repeated workflows in `scripts/`.

Keep `SKILL.md` concise. Prefer linking to references instead of embedding long guidance directly in the skill body.

## Validate The Skill

Run the official validator:

```bash
python3 /path/to/skill-creator/scripts/quick_validate.py .
```

Expected output:

```text
Skill is valid!
```

If `PyYAML` is missing, install it into a temporary directory:

```bash
python3 -m pip install PyYAML
```

## Test The Onboarding Script

Create a temporary target and run onboarding:

```bash
mkdir -p tmp/pragmatic-dev-skill-test
./scripts/onboard_project.py tmp/pragmatic-dev-skill-test --agent both --mode existing
```

Check generated files:

```bash
find tmp/pragmatic-dev-skill-test -maxdepth 3 -type f -print
```

Expected files include:

```text
tmp/pragmatic-dev-skill-test/.codex/project-profile.md
tmp/pragmatic-dev-skill-test/.codex/specs/system-overview.md
tmp/pragmatic-dev-skill-test/.codex/specs/_feature-template.md
tmp/pragmatic-dev-skill-test/AGENTS.md
tmp/pragmatic-dev-skill-test/CLAUDE.md
```

## Git Workflow

Before committing:

```bash
git status --short
git diff -- SKILL.md references scripts README.md agents
```

Then validate the skill and run a temporary onboarding script test.
