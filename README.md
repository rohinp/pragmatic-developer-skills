# Pragmatic Developer Skills

This repository contains complementary skills for AI coding agents:

- **Pragmatic Developer** guides ordinary software work toward simple, focused, maintainable, and verifiable changes.
- **High-Confidence Verification** selectively strengthens evidence for correctness-sensitive code through contracts, properties, invariants, models, static analysis, and focused formal techniques.

High-confidence verification is not the default for every change. The user may request it, repository instructions may require it, or the pragmatic skill may recommend it after identifying a material risk. Specialized tools or substantial verification work require user agreement.

## Repository Layout

```text
.
├── docs/
│   └── INSTALLATION.md
├── example/
│   ├── README.md
│   ├── semantic-caching-python/
│   ├── semantic-caching-scala/
│   └── semantic-caching-kotlin/
├── README.md
├── scripts/
│   └── install.sh
└── skills/
    ├── pragmatic-developer/
    │   ├── SKILL.md
    │   ├── agents/
    │   │   └── openai.yaml
    │   └── references/
    │       ├── pragmatic-engineering.md
    │       └── review-checklists.md
    └── high-confidence-verification/
        ├── SKILL.md
        ├── agents/
        │   └── openai.yaml
        └── references/
            ├── language-guidance.md
            ├── technique-selection.md
            └── verification-report.md
```

Each directory directly under `skills/` is a self-contained, independently installable skill. Repository-level documentation, examples, evaluation fixtures, and development tooling belong outside those runtime bundles.

## Skills

### Pragmatic Developer

Use `pragmatic-developer` for implementation, debugging, refactoring, design, and code review where careful scope, codebase alignment, and ordinary verification matter.

```text
Use $pragmatic-developer to implement this feature with the smallest compatible change.
```

### High-Confidence Verification

Use `high-confidence-verification` when stronger correctness evidence is explicitly requested or agreed after a risk discussion.

```text
Use $high-confidence-verification to assess this money-transfer rule and propose proportionate verification before adding specialized tools.
```

Its OpenAI metadata disables implicit invocation so it cannot silently activate in Codex. Other agents must preserve the same consent policy from its `SKILL.md`.

## Installation

Clone the repository and run the installer for your coding agent:

```bash
git clone https://github.com/rohinp/pragmatic-developer-skills.git
cd pragmatic-developer-skills
./scripts/install.sh --agent codex --scope user
```

Replace `codex` with `claude`, `copilot`, `gemini`, or `generic`. The command installs both skills and refuses to overwrite an existing installation.

> **Installation scope:** The example above explicitly uses `--scope user`. For Codex, this installs into `${CODEX_HOME:-$HOME/.codex}/skills`, making the skills available across projects. To keep the skills in one repository instead, run the installer from that repository root with `--agent codex --scope project`; it installs into `.agents/skills`. Use `--project-dir /path/to/repository` when running it from elsewhere.

Install only the normal engineering skill with:

```bash
./scripts/install.sh --agent codex --scope user --skill pragmatic-developer
```

See [Installation and onboarding](docs/INSTALLATION.md) for project-scoped installation, discovery paths, invocation syntax, verification, and client-specific guidance.

## Example Project

The [semantic and LLM request caching example](example/README.md) implements a deterministic graph-backed semantic cache in Python, Scala 3, and Kotlin. Its graph-caching concept is inspired by and credited to Manoj's HackerNoon article, [“Graph Theory-Based Semantic Caching: Scaling LLM Applications”](https://hackernoon.com/graph-theory-based-semantic-caching-scaling-llm-applications). It demonstrates how the pragmatic skill keeps infrastructure mocked and scope small while the verification skill selects contracts, domain types, graph invariants, property-based testing, and bounded exhaustive testing for concrete risks.

The example also records a defect found during implementation: validating a new embedding after mutating the graph could leave a partially inserted node. Candidate similarities are now validated before insertion in all three languages. This is evidence of a practical benefit, not a controlled with-versus-without-skill evaluation; that broader evaluation remains future work.

## Development

Keep each `SKILL.md` concise and put optional detail in its `references/` directory. Validate each directory under `skills/` independently after making changes.

## Future Verification Candidates

The current verification skill contains only techniques already exercised in the [Practical Formal Methods playground](https://github.com/rohinp/formal-methods-forgood). Possible future additions include:

- mutation testing;
- metamorphic testing;
- differential testing;
- fuzzing and adversarial input generation;
- fault injection, retries, and partial-failure properties;
- concurrency and interleaving testing.

These are research candidates, not current skill promises. Each technique should first receive a small executable experiment, developer-focused documentation, and an honest account of its guarantees and costs in the playground. It should enter `high-confidence-verification` only when the experiment demonstrates a useful failure mode and a clear risk-based trigger.

## Credits

The Pragmatic Developer behavioral foundation is adapted from [Karpathy-Inspired Claude Code Guidelines](https://github.com/multica-ai/andrej-karpathy-skills), published under the MIT License by the project authors and derived from Andrej Karpathy's observations about common LLM coding mistakes.

The skills also draw on established software-engineering ideas associated with Eric Evans, Kent Beck, Martin Fowler, Erich Gamma and collaborators, and Andrew Hunt and David Thomas. Their works are referenced as influences; no book text is distributed in this repository.
