#!/bin/sh

set -eu

usage() {
  cat <<'EOF'
Usage: ./scripts/install.sh --agent AGENT [options]

Install one or both skills for a supported coding agent.

Required:
  --agent codex|claude|copilot|gemini|generic

Options:
  --scope user|project       Installation scope (default: user)
  --project-dir PATH         Project root for project scope (default: current directory)
  --skill NAME|all           Skill to install (default: all)
  --destination PATH         Override the resolved skills directory
  -h, --help                 Show this help

The installer never overwrites an existing skill directory.
EOF
}

agent=""
scope="user"
project_dir=""
selected_skill="all"
destination=""

while [ "$#" -gt 0 ]; do
  case "$1" in
    --agent)
      [ "$#" -ge 2 ] || { echo "Missing value for --agent" >&2; exit 2; }
      agent="$2"
      shift 2
      ;;
    --scope)
      [ "$#" -ge 2 ] || { echo "Missing value for --scope" >&2; exit 2; }
      scope="$2"
      shift 2
      ;;
    --project-dir)
      [ "$#" -ge 2 ] || { echo "Missing value for --project-dir" >&2; exit 2; }
      project_dir="$2"
      shift 2
      ;;
    --skill)
      [ "$#" -ge 2 ] || { echo "Missing value for --skill" >&2; exit 2; }
      selected_skill="$2"
      shift 2
      ;;
    --destination)
      [ "$#" -ge 2 ] || { echo "Missing value for --destination" >&2; exit 2; }
      destination="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

case "$agent" in
  codex|claude|copilot|gemini|generic) ;;
  "")
    echo "--agent is required" >&2
    usage >&2
    exit 2
    ;;
  *)
    echo "Unsupported agent: $agent" >&2
    exit 2
    ;;
esac

case "$scope" in
  user|project) ;;
  *)
    echo "Unsupported scope: $scope" >&2
    exit 2
    ;;
esac

case "$selected_skill" in
  all)
    skill_names="pragmatic-developer high-confidence-verification"
    ;;
  pragmatic-developer|high-confidence-verification)
    skill_names="$selected_skill"
    ;;
  *)
    echo "Unsupported skill: $selected_skill" >&2
    exit 2
    ;;
esac

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
repository_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)

if [ -z "$destination" ]; then
  if [ "$scope" = "user" ]; then
    case "$agent" in
      codex)
        destination="${CODEX_HOME:-$HOME/.codex}/skills"
        ;;
      claude)
        destination="$HOME/.claude/skills"
        ;;
      copilot)
        destination="$HOME/.copilot/skills"
        ;;
      gemini)
        destination="$HOME/.gemini/skills"
        ;;
      generic)
        destination="$HOME/.agents/skills"
        ;;
    esac
  else
    if [ "$agent" = "codex" ]; then
      echo "Project-scoped Codex skill discovery is not configured by this installer." >&2
      echo "Use --scope user, or pass a verified directory with --destination." >&2
      exit 2
    fi

    if [ -z "$project_dir" ]; then
      project_dir=$(pwd)
    fi

    case "$agent" in
      claude)
        destination="$project_dir/.claude/skills"
        ;;
      copilot|gemini|generic)
        destination="$project_dir/.agents/skills"
        ;;
    esac
  fi
fi

for skill_name in $skill_names; do
  source_dir="$repository_dir/skills/$skill_name"
  target_dir="$destination/$skill_name"

  if [ ! -f "$source_dir/SKILL.md" ]; then
    echo "Invalid source skill: $source_dir" >&2
    exit 1
  fi

  if [ -e "$target_dir" ]; then
    echo "Refusing to overwrite existing skill: $target_dir" >&2
    echo "Remove or move it explicitly, then run this installer again." >&2
    exit 1
  fi
done

mkdir -p "$destination"

for skill_name in $skill_names; do
  cp -R "$repository_dir/skills/$skill_name" "$destination/$skill_name"
  echo "Installed $skill_name -> $destination/$skill_name"
done

