# Pragmatic Developer Skill

`pragmatic-developer` helps AI coding agents make careful, simple, and verifiable software changes. It combines a concise behavioral baseline with practical guidance for domain modeling, testing, refactoring, code review, and algorithmic decisions.

## Principles

1. **Think before coding.** Inspect the system, expose important assumptions, and clarify consequential ambiguity.
2. **Simplicity first.** Build only what is needed and avoid speculative abstractions.
3. **Surgical changes.** Follow local conventions and keep every changed line connected to the request.
4. **Goal-driven execution.** Define success, protect behavior with tests, and verify the result.

The skill applies SOLID, YAGNI, KISS, design patterns, and domain-driven design as diagnostic tools—not as reasons to add ceremony.

## Repository Layout

```text
.
├── SKILL.md
├── agents/
│   └── openai.yaml
└── references/
    ├── pragmatic-engineering.md
    └── review-checklists.md
```

- `SKILL.md` contains the always-loaded workflow and behavioral rules.
- `references/pragmatic-engineering.md` contains deeper engineering heuristics.
- `references/review-checklists.md` contains optional prompts for planning, review, and verification.
- `agents/openai.yaml` contains Codex UI metadata.

## Install

Clone the repository into your Codex skills directory:

```bash
git clone https://github.com/rohinp/pragmatic-developer-skills.git "${CODEX_HOME:-$HOME/.codex}/skills/pragmatic-developer"
```

Alternatively, copy this directory to the same destination.

Restart or reload Codex if the skill is not discovered immediately.

## Use

Invoke the skill explicitly:

```text
Use $pragmatic-developer to implement this feature with the smallest compatible change.
```

```text
Use $pragmatic-developer to review this refactor for behavior changes and unnecessary complexity.
```

```text
Use $pragmatic-developer to diagnose this bug and define a verifiable success condition.
```

The skill can also trigger automatically for software design, implementation, debugging, refactoring, and code-review tasks.

## Development

After changing the skill, run the validator from Codex's `skill-creator` package:

```bash
python3 /path/to/skill-creator/scripts/quick_validate.py .
```

Keep `SKILL.md` concise. Put detailed, occasionally needed guidance in `references/`, and add scripts only for repeated workflows that benefit from deterministic automation.

## Credits

The four-part behavioral foundation is adapted from [Karpathy-Inspired Claude Code Guidelines](https://github.com/multica-ai/andrej-karpathy-skills), published under the MIT License by the project authors and derived from Andrej Karpathy's observations about common LLM coding mistakes.

This skill extends that foundation with pragmatic software-engineering guidance informed by established ideas from Eric Evans, Kent Beck, Martin Fowler, Erich Gamma and collaborators, and Andrew Hunt and David Thomas. Their works are referenced as influences; no book text is distributed in this repository.
