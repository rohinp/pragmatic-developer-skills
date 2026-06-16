# Pragmatic Engineering Heuristics

## Domain Modeling

- Treat the model as a working theory of the business, not a diagram detached from code.
- Build a ubiquitous language: use the same terms in conversations, tests, APIs, modules, classes, functions, and database boundaries where practical.
- Resolve ambiguous names early. If two groups use the same word differently, consider a bounded context or explicit translation layer.
- Keep domain rules in named concepts. A rule such as eligibility, pricing, routing, allocation, authorization, or scheduling usually deserves a named policy, specification, value object, or domain service.
- Separate domain decisions from application orchestration, persistence, transport, and UI formatting.
- Use entities for identity and lifecycle. Use value objects for immutable descriptive values with equality by value. Use aggregates to protect consistency boundaries. Use repositories to retrieve aggregate roots by domain intent, not as generic database wrappers.
- Favor small models that solve the current problem. Avoid modeling every real-world detail before it affects behavior.
- When the model feels awkward, use that discomfort as feedback: rename, split concepts, move behavior, or ask a domain question.

## Clean Code

- Make names carry domain intent and operational intent. Prefer `approveInvoice` over `process`, and `OverbookingPolicy` over a hidden percentage check.
- Keep functions small enough that their control flow and side effects are visible.
- Make dependencies explicit. Avoid hidden global state and temporal coupling unless the codebase already has a constrained pattern for it.
- Remove duplication after the abstraction is understood. Premature abstraction can freeze a weak model.
- Prefer simple composition to inheritance unless subtype substitution is truly central to the design.
- Keep comments for non-obvious context, tradeoffs, constraints, or links to domain decisions. Do not comment what the code already says.
- Make errors explicit and actionable. Preserve causes when wrapping exceptions.

## SOLID Principles

Use SOLID as a diagnostic and refactoring vocabulary. Do not apply it mechanically or create abstractions before the code has a real variation point.

### Single Responsibility Principle

- A module, class, or function should have one primary reason to change.
- Split responsibilities when changes for unrelated reasons are tangled together, tests require excessive setup, or names become vague.
- Keep cohesive responsibilities together; do not split so far that behavior becomes hard to follow.

### Open-Closed Principle

- Prefer designs where common new behavior can be added through extension rather than repeatedly editing stable code.
- Apply this when variation is real or likely from current requirements, not merely imagined.
- Good candidates: business policies, strategies, validators, serializers, handlers, or adapters with multiple known implementations.
- Avoid speculative plugin systems or inheritance trees just to satisfy OCP.

### Liskov Substitution Principle

- Subtypes must honor the behavioral contract of their base type or interface.
- Watch for subclasses that throw unsupported-operation errors, weaken invariants, strengthen preconditions, change return meanings, or surprise callers.
- If substitution is not valid, prefer composition, separate interfaces, or explicit domain concepts.

### Interface Segregation Principle

- Clients should depend only on the behavior they use.
- Split broad interfaces when implementations contain irrelevant methods, callers need only narrow capabilities, or tests require fake methods unrelated to the behavior under test.
- Avoid tiny one-method interfaces everywhere unless they reduce real coupling in the local codebase.

### Dependency Inversion Principle

- High-level policy should not depend directly on low-level details when that coupling makes testing, replacement, or domain clarity harder.
- Depend on abstractions at boundaries such as persistence, external services, clocks, messaging, payment providers, and file systems.
- Keep abstractions owned by the domain/application side when possible; do not let infrastructure define the core model.
- For simple stable dependencies, direct use can be more pragmatic than an unnecessary wrapper.

## YAGNI And KISS

- YAGNI: implement functionality when it is actually needed, not because it might be needed later.
- KISS: prefer the simplest design that satisfies the current behavior, constraints, and maintainability needs.
- Use these with tests and refactoring. Simple today should remain easy to improve tomorrow.
- Remove or defer unused extension points, generic frameworks, broad configuration, premature caching, unused interfaces, and speculative data model fields.
- Do not use YAGNI to ignore clear near-term requirements, public contracts, migrations, security, data compatibility, or expensive-to-change decisions.
- Do not use KISS as an excuse for simplistic code that hides domain rules or spreads duplication across the system.

## Test-Driven Development

- Use red-green-refactor as a design rhythm, not a ceremony.
- Start from behavior visible to the caller. A good test states a business rule, protocol expectation, or algorithmic edge case.
- Use quick green tactics when needed:
  - Fake implementation: return a constant, then generalize.
  - Obvious implementation: write the direct solution when confidence is high.
  - Triangulation: add examples until the abstraction is forced.
- Translate design concerns into tests. Examples: mutation concerns become repeated-call tests; equality concerns become value comparisons; boundary concerns become min/max tests.
- Keep test data meaningful in the domain. Use builders or fixtures only when they reduce noise.
- During legacy work, add characterization tests before changing behavior.

## Refactoring

- Refactoring changes structure without changing observable behavior. Say when you are refactoring and when you are changing behavior.
- Keep steps small and verifiable. Rename, extract, inline, move, split, and simplify with tests between risky moves.
- Use code smells as prompts, not rules: duplicated code, long function, large class, long parameter list, feature envy, shotgun surgery, divergent change, primitive obsession, switch statements, temporary fields, message chains, middle man, speculative generality, and data clumps.
- Prefer refactorings that reveal domain concepts: extract policy, introduce value object, move behavior to the object that owns the data, replace primitive with domain type, split bounded contexts, and rename to the ubiquitous language.
- Do not mix sweeping refactors with urgent feature changes unless the feature cannot be implemented safely otherwise.

## Design Patterns

- Use patterns to communicate design intent and manage change points.
- Prefer Strategy or policy objects for interchangeable business rules or algorithms.
- Prefer Observer or event publication when dependents need notification without tight coupling.
- Prefer Decorator for runtime responsibilities around a stable interface.
- Prefer Factory Method or Abstract Factory when construction details would otherwise leak concrete dependencies.
- Prefer Command when invocation must be queued, logged, undone, retried, or transported.
- Prefer Adapter to translate external or legacy interfaces. Prefer Facade to simplify a complex subsystem for a client.
- Prefer Template Method only when a stable algorithm skeleton genuinely belongs in a base type. Consider Strategy first when composition would keep variation clearer.
- Avoid pattern fever. A pattern is useful only when it removes coupling, clarifies responsibility, or makes a real variation point explicit.

## Algorithmic Judgment

- Start by understanding the data: size, distribution, sortedness, uniqueness, mutation rate, latency expectations, and memory limits.
- Make complexity explicit for hot paths and public utilities. State time and space complexity when it shapes the implementation.
- Use standard data structures deliberately: hash maps for lookup, sets for membership, heaps for top-k or priority, queues for breadth-first work, stacks for depth-first work, tries for prefix search, sorted arrays or trees for ordered operations.
- Check correctness boundaries: empty input, one item, duplicates, null or missing fields, invalid values, maximum size, overflow, stable ordering, and concurrent access.
- Prefer readable O(n log n) over fragile O(n) unless the improvement matters. Optimize with measurements or clear constraints.
- Keep algorithm code testable with deterministic inputs and explicit tie-breaking.

## Pragmatic Delivery

- Optimize for changeability in the parts of the system that are actively changing.
- Keep reversible decisions lightweight. Spend more design energy on decisions that are expensive to change: public APIs, data models, persistence boundaries, security, consistency, and distributed contracts.
- Use tracer bullets or thin vertical slices to learn from running software before committing to broad architecture.
- Keep quality good enough for the context, but do not normalize broken windows: small visible neglect compounds.
- Communicate assumptions, tradeoffs, and verification plainly.

## Existing Repository Alignment

- Infer first, then improve. Read the local codebase deeply enough to understand what it values before applying general advice.
- Treat existing patterns as constraints unless they are actively causing defects, confusion, or high change cost.
- Prefer compatible refactorings that improve the current design vocabulary over imported architecture styles.
- When recommending a different design, present it as an option with migration cost, risk, and expected payoff.
- Record user-approved direction changes in the project profile so future work follows the same choice.

## Greenfield Preference Building

- Make early choices explicit because there is no established codebase to read from.
- Keep early architecture modest: one vertical slice, clear tests, and named domain concepts.
- Record provisional choices in the project profile so they can evolve deliberately instead of becoming accidental conventions.
- Reassess provisional choices when new constraints appear, such as persistence complexity, deployment needs, concurrency, performance, or team workflow.
