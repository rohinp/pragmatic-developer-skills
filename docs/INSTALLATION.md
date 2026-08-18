# Installation and Onboarding

Both directories under `skills/` follow the open Agent Skills structure. Install either skill independently or install both together.

## Choose the Skills

- Install `pragmatic-developer` for normal design, implementation, debugging, refactoring, and review work.
- Install `high-confidence-verification` when you want the option of stronger, risk-based verification.
- Installing the verification skill does not authorize silent formal-methods work. Its instructions require an explicit request, a repository mandate, or a user-approved recommendation.

## Installer

From the repository root:

```bash
./scripts/install.sh --agent codex
./scripts/install.sh --agent claude
./scripts/install.sh --agent copilot
./scripts/install.sh --agent gemini
./scripts/install.sh --agent generic
```

These commands install both skills at user scope. Install only one with:

```bash
./scripts/install.sh --agent codex --skill pragmatic-developer
./scripts/install.sh --agent codex --skill high-confidence-verification
```

For a repository-scoped installation:

```bash
./scripts/install.sh --agent claude --scope project --project-dir /path/to/project
./scripts/install.sh --agent copilot --scope project --project-dir /path/to/project
./scripts/install.sh --agent gemini --scope project --project-dir /path/to/project
./scripts/install.sh --agent generic --scope project --project-dir /path/to/project
```

The installer refuses to overwrite an existing skill. Review and remove or move the existing directory yourself before reinstalling.

## Client Matrix

| Client | Personal location | Project location | Explicit use or discovery check |
|---|---|---|---|
| Codex | `${CODEX_HOME:-$HOME/.codex}/skills/<name>` | Use personal scope unless your Codex environment documents another location | `Use $pragmatic-developer ...` |
| Claude Code | `~/.claude/skills/<name>` | `.claude/skills/<name>` | `/pragmatic-developer` or `/high-confidence-verification` |
| GitHub Copilot | `~/.copilot/skills/<name>` or `~/.agents/skills/<name>` | `.github/skills/<name>`, `.claude/skills/<name>`, or `.agents/skills/<name>` | Ask Copilot to use the named skill; Copilot CLI supports `/skills reload` |
| Gemini CLI | `~/.gemini/skills/<name>` or `~/.agents/skills/<name>` | `.gemini/skills/<name>` or `.agents/skills/<name>` | `/skills list`, then request the named skill |
| Generic Agent Skills client | Client-defined or `~/.agents/skills/<name>` when supported | `.agents/skills/<name>` when supported | Ask the client to use the skill by its `name` |

`agents/openai.yaml` is optional OpenAI-specific metadata. Clients that implement only the open standard can ignore it and use `SKILL.md` plus its referenced resources.

## Codex

Install both skills:

```bash
./scripts/install.sh --agent codex
```

Restart or reload Codex if the newly created top-level skill directories are not discovered in the current session. Invoke explicitly with:

```text
Use $pragmatic-developer to implement this change with the smallest compatible design.
```

```text
Use $high-confidence-verification to assess this transfer rule and propose proportionate verification.
```

The OpenAI metadata for `high-confidence-verification` disables implicit invocation. The pragmatic skill may still recommend it when risk warrants discussion.

## Claude Code

Install personally:

```bash
./scripts/install.sh --agent claude
```

Or install into a project:

```bash
./scripts/install.sh --agent claude --scope project --project-dir /path/to/project
```

Claude Code discovers skills from `~/.claude/skills` and `.claude/skills`. Invoke them as `/pragmatic-developer` and `/high-confidence-verification`. Claude may also select a relevant skill from its description; the verification skill's internal gate still forbids silently adding specialized verification.

## GitHub Copilot

Install personally:

```bash
./scripts/install.sh --agent copilot
```

For a shared repository installation, the installer uses the vendor-neutral `.agents/skills` location:

```bash
./scripts/install.sh --agent copilot --scope project --project-dir /path/to/project
```

Copilot also recognizes `.github/skills` and `.claude/skills` for project skills, and `~/.agents/skills` for personal skills. Start a new session or use `/skills reload` in Copilot CLI after installation.

## Gemini CLI

Install personally:

```bash
./scripts/install.sh --agent gemini
```

For a shared workspace installation, the installer uses `.agents/skills`:

```bash
./scripts/install.sh --agent gemini --scope project --project-dir /path/to/project
```

Gemini also provides native development linking:

```bash
gemini skills link ./skills/pragmatic-developer
gemini skills link ./skills/high-confidence-verification
```

Use `/skills list` to verify discovery and `/skills reload` after changes. Gemini asks for activation consent when a skill is triggered.

## Generic Agent Skills Clients

The most portable project layout is:

```text
.agents/
  skills/
    pragmatic-developer/
      SKILL.md
    high-confidence-verification/
      SKILL.md
```

Install it with:

```bash
./scripts/install.sh --agent generic --scope project --project-dir /path/to/project
```

If a client uses a different discovery directory, pass it explicitly:

```bash
./scripts/install.sh --agent generic --destination /path/to/client/skills
```

Confirm that the client supports the [Agent Skills specification](https://agentskills.io/specification) and follow its precedence, reload, and invocation rules.

## Verify the Installation

Check that each installed directory contains `SKILL.md`:

```text
<skills-directory>/pragmatic-developer/SKILL.md
<skills-directory>/high-confidence-verification/SKILL.md
```

Then try a low-risk prompt:

```text
Use pragmatic-developer to review this small refactor for unnecessary scope.
```

For the verification skill, begin with assessment rather than automatic implementation:

```text
Use high-confidence-verification to assess whether this code warrants stronger verification. Recommend a technique and wait for my agreement before adding specialized tools.
```

## Authoritative References

- [Agent Skills specification](https://agentskills.io/specification)
- [OpenAI Skills documentation](https://developers.openai.com/api/docs/guides/tools-skills)
- [Claude Code skills](https://code.claude.com/docs/en/slash-commands)
- [GitHub Copilot agent skills](https://docs.github.com/en/copilot/concepts/agents/about-agent-skills)
- [Gemini CLI agent skills](https://geminicli.com/docs/cli/using-agent-skills/)

