# Review And Implementation Checklists

Use these as prompts. Do not paste the whole checklist into user-facing responses.

## First Pass

- What behavior is the user asking to add, remove, preserve, or explain?
- Is this an existing repository or a greenfield project?
- Is there a project profile? If yes, what choices or preferences apply?
- Which files, tests, and boundaries already own this behavior?
- What domain words appear in code, tests, docs, tickets, or data?
- Is there existing style for errors, validation, persistence, logging, async work, and tests?
- What is the smallest change that proves progress?

## Project Profile Review

- Does `.codex/project-profile.md`, `docs/project-profile.md`, `PROJECT_PROFILE.md`, or equivalent repo guidance exist?
- Are observed conventions separated from user decisions?
- Did the user choose between alternatives during this task?
- Should that choice be recorded as durable, provisional, rejected, superseded, or open?
- Does the current recommendation conflict with a recorded choice?
- Does the final response need to mention a profile update?

## Domain Model Review

- Does each important business rule have a clear name?
- Are invariants enforced in one place close to the model they protect?
- Are entities, value objects, services, repositories, and policies used intentionally?
- Is an application/service layer making domain decisions that belong in the domain?
- Are persistence models leaking into domain decisions?
- Are two bounded contexts being forced into one vocabulary?
- Are translations to external systems explicit and tested?

## Clean Code Review

- Can a reader understand the change from names and tests?
- Is there needless indirection, speculative abstraction, or pattern use?
- Is duplication accidental, or does it reveal a missing domain concept?
- Are side effects isolated and visible?
- Are error paths handled consistently?
- Are public APIs, database schemas, or contracts changed deliberately?
- Can this code be safely modified in six months?

## TDD And Test Review

- Is there a failing test or characterization test for the behavior?
- Do tests assert behavior rather than private implementation details?
- Are edge cases covered for boundaries, invalid input, repeated calls, concurrency, and ordering where relevant?
- Are fixtures readable and domain-meaningful?
- Do tests fail for the right reason when the behavior is broken?
- Is the verification scope proportional to the blast radius?

## Refactoring Review

- Is the refactor behavior-preserving?
- Are tests in place before moving behavior?
- Are behavior changes separated from mechanical cleanup where practical?
- Did renames improve domain language?
- Did extracted functions/classes reduce complexity or just move it?
- Did the refactor remove a real change hazard?

## Algorithm Review

- What are the expected input sizes and growth pattern?
- What are the time and space complexity costs?
- Is the data structure choice obvious and appropriate?
- Are ordering, duplicates, ties, and missing values handled?
- Are there overflow, precision, or concurrency risks?
- Is performance assumed, measured, or derived from clear constraints?

## Final Response

- State the concrete change.
- State why it fits the domain/design.
- State whether the change follows or updates the project profile.
- State tests or checks run.
- State tests not run or residual risks.
- Keep follow-up suggestions limited to work that directly builds on the change.
