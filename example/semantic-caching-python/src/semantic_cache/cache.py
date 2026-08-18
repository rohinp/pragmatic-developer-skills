from __future__ import annotations

from dataclasses import dataclass
from math import sqrt
from typing import Protocol

Embedding = tuple[float, ...]


@dataclass(frozen=True, order=True)
class QueryId:
    value: int

    def __post_init__(self) -> None:
        if self.value <= 0:
            raise ValueError("query id must be positive")


@dataclass(frozen=True, order=True)
class Similarity:
    value: float

    def __post_init__(self) -> None:
        if not 0.0 <= self.value <= 1.0:
            raise ValueError("similarity must be between zero and one")


@dataclass(frozen=True)
class CacheConfig:
    edge_threshold: Similarity = Similarity(0.20)
    hit_threshold: Similarity = Similarity(0.90)
    candidate_limit: int = 10
    search_start_limit: int = 3
    neighbors_per_start: int = 2

    def __post_init__(self) -> None:
        if self.hit_threshold < self.edge_threshold:
            raise ValueError("hit threshold must be at least the edge threshold")
        if min(self.candidate_limit, self.search_start_limit, self.neighbors_per_start) <= 0:
            raise ValueError("all traversal limits must be positive")


@dataclass(frozen=True)
class CacheNode:
    id: QueryId
    query: str
    response: str
    embedding: Embedding


@dataclass(frozen=True)
class SearchResult:
    node: CacheNode | None
    similarity: Similarity | None
    inspected_nodes: int


@dataclass(frozen=True)
class CacheOutcome:
    response: str
    cache_hit: bool
    inspected_nodes: int


class EmbeddingService(Protocol):
    def embed(self, query: str) -> Embedding: ...


class LlmService(Protocol):
    def generate(self, query: str) -> str: ...


def cosine_similarity(left: Embedding, right: Embedding) -> Similarity:
    if not left or len(left) != len(right):
        raise ValueError("embeddings must be non-empty and have equal dimensions")
    if any(value < 0.0 for value in left + right):
        raise ValueError("this example models non-negative embeddings only")

    left_norm = sqrt(sum(value * value for value in left))
    right_norm = sqrt(sum(value * value for value in right))
    if left_norm == 0.0 or right_norm == 0.0:
        raise ValueError("embeddings must have non-zero magnitude")

    raw = sum(a * b for a, b in zip(left, right, strict=True)) / (left_norm * right_norm)
    return Similarity(min(1.0, max(0.0, raw)))


class SemanticGraph:
    def __init__(self, config: CacheConfig = CacheConfig()) -> None:
        self._config = config
        self._nodes: dict[QueryId, CacheNode] = {}
        self._edges: dict[QueryId, dict[QueryId, Similarity]] = {}
        self._recent: list[QueryId] = []
        self._next_id = 1

    @property
    def size(self) -> int:
        return len(self._nodes)

    def add(self, query: str, response: str, embedding: Embedding) -> CacheNode:
        if not query.strip():
            raise ValueError("query must not be blank")
        cosine_similarity(embedding, embedding)

        candidate_scores = [
            (candidate_id, cosine_similarity(embedding, self._nodes[candidate_id].embedding))
            for candidate_id in self._recent[: self._config.candidate_limit]
        ]

        node_id = QueryId(self._next_id)
        self._next_id += 1
        node = CacheNode(node_id, query, response, embedding)
        self._nodes[node_id] = node
        self._edges[node_id] = {}

        for candidate_id, similarity in candidate_scores:
            if similarity >= self._config.edge_threshold:
                self._edges[node_id][candidate_id] = similarity
                self._edges[candidate_id][node_id] = similarity

        self._recent.insert(0, node_id)
        self.assert_invariants()
        return node

    def find(self, embedding: Embedding) -> SearchResult:
        cosine_similarity(embedding, embedding)
        candidate_ids: set[QueryId] = set()

        for start_id in self._recent[: self._config.search_start_limit]:
            candidate_ids.add(start_id)
            strongest = sorted(
                self._edges[start_id].items(),
                key=lambda item: (-item[1].value, item[0].value),
            )[: self._config.neighbors_per_start]
            candidate_ids.update(neighbor_id for neighbor_id, _ in strongest)

        scored = [
            (cosine_similarity(embedding, self._nodes[node_id].embedding), node_id)
            for node_id in candidate_ids
        ]
        eligible = [item for item in scored if item[0] >= self._config.hit_threshold]
        if not eligible:
            return SearchResult(None, None, len(candidate_ids))

        similarity, node_id = max(eligible, key=lambda item: (item[0].value, -item[1].value))
        return SearchResult(self._nodes[node_id], similarity, len(candidate_ids))

    def neighbors(self, node_id: QueryId) -> dict[QueryId, Similarity]:
        return dict(self._edges[node_id])

    def assert_invariants(self) -> None:
        if set(self._nodes) != set(self._edges):
            raise AssertionError("every node must own an adjacency map")
        if len(self._recent) != len(set(self._recent)) or set(self._recent) != set(self._nodes):
            raise AssertionError("recent nodes must contain every node exactly once")

        for source, neighbors in self._edges.items():
            for target, weight in neighbors.items():
                if source == target:
                    raise AssertionError("self-loops are forbidden")
                if target not in self._nodes:
                    raise AssertionError("edge target must exist")
                if weight < self._config.edge_threshold:
                    raise AssertionError("edge weight must satisfy the threshold")
                if self._edges[target].get(source) != weight:
                    raise AssertionError("edges must be symmetric with equal weights")


class SemanticCache:
    def __init__(
        self,
        embeddings: EmbeddingService,
        llm: LlmService,
        config: CacheConfig = CacheConfig(),
    ) -> None:
        self._embeddings = embeddings
        self._llm = llm
        self._graph = SemanticGraph(config)

    @property
    def graph(self) -> SemanticGraph:
        return self._graph

    def request(self, query: str) -> CacheOutcome:
        if not query.strip():
            raise ValueError("query must not be blank")

        embedding = self._embeddings.embed(query)
        cached = self._graph.find(embedding)
        if cached.node is not None:
            return CacheOutcome(cached.node.response, True, cached.inspected_nodes)

        response = self._llm.generate(query)
        self._graph.add(query, response, embedding)
        return CacheOutcome(response, False, cached.inspected_nodes)


class DeterministicEmbeddingService:
    _INTENTS: tuple[tuple[set[str], Embedding], ...] = (
        ({"password", "reset", "recovery", "forgot"}, (1.0, 0.0, 0.0)),
        ({"invoice", "billing", "payment", "receipt"}, (0.0, 1.0, 0.0)),
        ({"weather", "rain", "temperature", "forecast"}, (0.0, 0.0, 1.0)),
    )

    def embed(self, query: str) -> Embedding:
        words = {word.strip("?.,!").lower() for word in query.split()}
        vector = tuple(
            sum(1.0 for word in words if word in vocabulary)
            for vocabulary, _ in self._INTENTS
        )
        if any(vector):
            return vector
        return (1.0, 1.0, 1.0)


class CountingLlmService:
    def __init__(self) -> None:
        self.call_count = 0

    def generate(self, query: str) -> str:
        self.call_count += 1
        return f"mock-response:{query}"
