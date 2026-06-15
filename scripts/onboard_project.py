#!/usr/bin/env python3
"""Initialize a target repo for the pragmatic-developer workflow."""

from __future__ import annotations

import argparse
import datetime as dt
from pathlib import Path
import textwrap


MARKER_START = "<!-- pragmatic-developer:start -->"
MARKER_END = "<!-- pragmatic-developer:end -->"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create or update a project profile and optional Codex/Claude guidance files."
    )
    parser.add_argument("project_path", help="Target project/repository path.")
    parser.add_argument(
        "--agent",
        choices=["codex", "claude", "both", "none"],
        default="both",
        help="Which agent guidance files to create or update. Default: both.",
    )
    parser.add_argument(
        "--mode",
        choices=["existing", "greenfield", "legacy-modernization", "maintenance"],
        default="existing",
        help="Project maturity to record in a new profile. Default: existing.",
    )
    parser.add_argument(
        "--custom-profile",
        help="Optional user-provided profile, preferences, or architecture notes to import.",
    )
    parser.add_argument(
        "--profile-path",
        default=".codex/project-profile.md",
        help="Profile path relative to the target project. Default: .codex/project-profile.md.",
    )
    parser.add_argument(
        "--spec-dir",
        default=".codex/specs",
        help="Specs directory relative to the target project. Default: .codex/specs.",
    )
    parser.add_argument(
        "--no-specs",
        action="store_true",
        help="Do not create spec placeholders.",
    )
    return parser.parse_args()


def today() -> str:
    return dt.date.today().isoformat()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def append_if_missing(path: Path, content: str, marker: str) -> bool:
    existing = read_text(path) if path.exists() else ""
    if marker in existing:
        return False
    prefix = "" if not existing else "\n\n"
    write_text(path, existing + prefix + content.rstrip() + "\n")
    return True


def profile_template(mode: str) -> str:
    mode_label = {
        "existing": "Existing system",
        "greenfield": "Greenfield",
        "legacy-modernization": "Legacy modernization",
        "maintenance": "Maintenance",
    }[mode]

    return textwrap.dedent(
        f"""\
        # Project Profile

        Last updated: {today()}

        Purpose: Durable engineering context for AI agents working in this repository.

        ## Project Context

        - Domain:
        - Product/runtime:
        - Current maturity: {mode_label}
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
        """
    )


def imported_profile_section(source: Path, content: str) -> str:
    return (
        "\n\n"
        "## Imported User Profile\n\n"
        f"Imported on: {today()}\n\n"
        f"Source: `{source}`\n\n"
        "Status: User provided. Treat this as durable input and normalize it into the structured sections above as decisions become clear.\n\n"
        "````markdown\n"
        f"{content.rstrip()}\n"
        "````\n"
    )


def agent_guidance(agent_name: str, profile_path: str) -> str:
    spec_dir = agent_guidance.spec_dir
    return textwrap.dedent(
        f"""\
        {MARKER_START}
        # Pragmatic Developer Guidance

        Use the `pragmatic-developer` skill for domain modeling, clean code, TDD, refactoring, design patterns, algorithmic tradeoffs, and project preference tracking.

        Before changing design direction:

        - Read `{profile_path}`.
        - Read relevant specs in `{spec_dir}` before changing behavior.
        - For an existing codebase, infer and follow observed architecture, naming, test style, and design patterns before suggesting alternatives.
        - For existing projects, fill spec placeholders gradually from code, tests, docs, and user confirmation. Do not invent behavior.
        - For greenfield work, record provisional choices in `{profile_path}` and update them as the project matures.
        - When the user chooses between design options, update `{profile_path}` with the decision, rationale, and scope.
        - When expected behavior, acceptance criteria, domain rules, API contracts, data behavior, or test proof changes, update the relevant spec in `{spec_dir}`.
        - Keep code smell fixes compatible with the current repo first; present larger design alternatives with tradeoffs.

        This guidance is intended for {agent_name}.
        {MARKER_END}
        """
    )


agent_guidance.spec_dir = ".codex/specs"


def system_spec_template(mode: str) -> str:
    status = "Placeholder" if mode != "greenfield" else "Draft"
    return textwrap.dedent(
        f"""\
        # System Overview Spec

        Last updated: {today()}

        Status: {status}

        ## Purpose

        - What should the system do? TBD
        - Primary users: TBD
        - Primary outcomes: TBD

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
        """
    )


def feature_spec_template() -> str:
    return textwrap.dedent(
        f"""\
        # Feature Spec Template

        Last updated: {today()}

        Status: Placeholder

        Copy this file to `<feature-name>.md` when a feature or change needs behavioral clarity.

        ## Goal

        - What should this feature do? TBD
        - What problem does it solve? TBD

        ## User Flows

        1. TBD

        ## Acceptance Criteria

        - [ ] TBD

        ## Domain Rules And Edge Cases

        - TBD

        ## API, Contract, And Data Behavior

        - Inputs: TBD
        - Outputs: TBD
        - Errors: TBD
        - Persistence/data changes: TBD
        - Backward compatibility: TBD

        ## Test Proof

        - Existing tests: TBD
        - New tests needed: TBD
        - Characterization tests needed: TBD

        ## Dependencies And Constraints

        - TBD

        ## Open Questions

        - TBD
        """
    )


def ensure_profile(project: Path, relative_profile: str, mode: str, custom_profile: Path | None) -> list[str]:
    actions: list[str] = []
    profile_path = project / relative_profile

    if profile_path.exists():
        actions.append(f"kept existing profile: {profile_path}")
    else:
        write_text(profile_path, profile_template(mode))
        actions.append(f"created profile: {profile_path}")

    if custom_profile:
        custom_profile = custom_profile.expanduser().resolve()
        custom_text = read_text(custom_profile)
        section = imported_profile_section(custom_profile, custom_text)
        changed = append_if_missing(profile_path, section, f"Source: `{custom_profile}`")
        if changed:
            actions.append(f"imported custom profile: {custom_profile}")
        else:
            actions.append(f"custom profile already imported: {custom_profile}")

    return actions


def ensure_specs(project: Path, relative_spec_dir: str, mode: str) -> list[str]:
    actions: list[str] = []
    spec_dir = project / relative_spec_dir
    system_spec = spec_dir / "system-overview.md"
    feature_template = spec_dir / "_feature-template.md"

    if system_spec.exists():
        actions.append(f"kept existing system spec: {system_spec}")
    else:
        write_text(system_spec, system_spec_template(mode))
        actions.append(f"created system spec placeholder: {system_spec}")

    if feature_template.exists():
        actions.append(f"kept existing feature spec template: {feature_template}")
    else:
        write_text(feature_template, feature_spec_template())
        actions.append(f"created feature spec template: {feature_template}")

    return actions


def ensure_agent_files(project: Path, agent: str, profile_path: str, spec_dir: str) -> list[str]:
    actions: list[str] = []
    agent_guidance.spec_dir = spec_dir
    targets: list[tuple[str, str]] = []
    if agent in {"codex", "both"}:
        targets.append(("AGENTS.md", "Codex"))
    if agent in {"claude", "both"}:
        targets.append(("CLAUDE.md", "Claude"))

    for filename, name in targets:
        path = project / filename
        changed = append_if_missing(path, agent_guidance(name, profile_path), MARKER_START)
        actions.append(("created/updated" if changed else "kept existing") + f" guidance: {path}")

    return actions


def main() -> int:
    args = parse_args()
    project = Path(args.project_path).expanduser().resolve()
    if not project.exists() or not project.is_dir():
        raise SystemExit(f"Target project does not exist or is not a directory: {project}")

    custom_profile = Path(args.custom_profile) if args.custom_profile else None
    if custom_profile and not custom_profile.expanduser().exists():
        raise SystemExit(f"Custom profile does not exist: {custom_profile}")

    actions = []
    actions.extend(ensure_profile(project, args.profile_path, args.mode, custom_profile))
    if not args.no_specs:
        actions.extend(ensure_specs(project, args.spec_dir, args.mode))
    if args.agent != "none":
        actions.extend(ensure_agent_files(project, args.agent, args.profile_path, args.spec_dir))

    print("Onboarding complete:")
    for action in actions:
        print(f"- {action}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
