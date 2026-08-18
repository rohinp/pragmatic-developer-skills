# Semantic and LLM Request Caching Example

This example applies the repository's two skills to a realistic graph-backed
semantic cache inspired by [Graph Theory-Based Semantic Caching: Scaling LLM
Applications](https://hackernoon.com/graph-theory-based-semantic-caching-scaling-llm-applications).

The article uses Redis, embeddings, and an LLM API. This repository replaces
those external services with deterministic in-memory mocks so that the same
behavior and verification ideas can run in Python, Scala 3, and Kotlin without
credentials or infrastructure.

## Implementations

- `semantic-caching-python/`
- `semantic-caching-scala/`
- `semantic-caching-kotlin/`

Each implementation keeps production code under `src` and groups tests by
verification technique.

## Model

Each cached request is a graph node containing its query, response, and
embedding. Similar nodes are joined by weighted, bidirectional edges. Lookup
starts from recent nodes, follows their strongest edges, and compares the new
query embedding with every visited node.

The example deliberately separates two thresholds:

- the lower **edge threshold** decides whether cached nodes are related enough
  to connect;
- the higher **hit threshold** decides whether reusing a cached response is
  safe enough for this simplified domain.

## Verified Properties

The tests provide evidence for these specific claims:

1. cosine similarity is defined only for compatible, non-zero embeddings and
   always lies between zero and one for the non-negative vectors used here;
2. every edge references existing nodes, has no self-loop, is bidirectional,
   has equal weight in both directions, and satisfies the edge threshold;
3. graph traversal inspects no more than
   `search starts × (1 + neighbors per start)` distinct nodes;
4. a semantic cache hit does not invoke the mocked LLM or create a new node;
5. exhaustive request sequences over three modeled intents call the LLM once
   per distinct intent.

These checks do **not** prove that a real embedding model understands meaning,
that approximate graph search always finds the globally best match, or that a
Redis/distributed implementation is race-free.

## What the Skills Changed

`pragmatic-developer` kept the first slice in memory, injected the external
services, and avoided adding Redis or a real model before those dependencies are
needed. `high-confidence-verification` selected techniques according to the
cache's failure modes instead of applying every method in its catalog.

That review found a concrete partial-update defect in the initial Python design:
an incompatible embedding dimension could fail while edges were being computed,
after the new node had already been inserted. All three implementations now
validate candidate similarities before mutating graph state, and a contract test
protects that behavior.

This is a worked demonstration, not yet a controlled agent evaluation. A future
evaluation should give agents the same incomplete starting project and compare
their raw diffs, test evidence, unnecessary dependencies, and surviving seeded
defects with and without the skills.

## Credit

The graph-based semantic-caching concept and motivating password-recovery
example come from Manoj's August 5, 2025 HackerNoon article,
[“Graph Theory-Based Semantic Caching: Scaling LLM Applications”](https://hackernoon.com/graph-theory-based-semantic-caching-scaling-llm-applications).
The Python, Scala 3, and Kotlin implementations and their formal-method tests in
this repository were written independently for this educational example.

## Run

### Python

```bash
cd semantic-caching-python
python -m venv .venv
.venv/bin/pip install -r requirements-dev.txt
PYTHONPATH=src .venv/bin/python -m unittest discover -s tests -p 'test_*.py' -v
.venv/bin/mypy --config-file pyproject.toml
```

### Scala 3

```bash
cd semantic-caching-scala
sbt test
```

### Kotlin

```bash
cd semantic-caching-kotlin
./gradlew test --rerun
```
