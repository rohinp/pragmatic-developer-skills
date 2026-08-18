import unittest

from hypothesis import given
from hypothesis import strategies as st

from semantic_cache import (
    CacheConfig,
    CountingLlmService,
    DeterministicEmbeddingService,
    SemanticCache,
    SemanticGraph,
    Similarity,
    cosine_similarity,
)


components = st.one_of(
    st.just(0.0),
    st.floats(
        min_value=1e-6,
        max_value=100.0,
        allow_nan=False,
        allow_infinity=False,
        allow_subnormal=False,
    ),
)

positive_vectors = st.lists(
    components,
    min_size=1,
    max_size=6,
).filter(lambda values: any(value > 0.0 for value in values))


@st.composite
def compatible_vector_lists(draw: st.DrawFn) -> list[list[float]]:
    dimension = draw(st.integers(min_value=1, max_value=6))
    vector = st.lists(components, min_size=dimension, max_size=dimension).filter(
        lambda values: any(value > 0.0 for value in values)
    )
    return draw(st.lists(vector, min_size=1, max_size=12))


class CachePropertiesTest(unittest.TestCase):
    @given(positive_vectors)
    def test_cosine_is_reflexive_and_bounded(self, values: list[float]) -> None:
        vector = tuple(values)
        similarity = cosine_similarity(vector, vector)
        self.assertAlmostEqual(similarity.value, 1.0)
        self.assertGreaterEqual(similarity.value, 0.0)
        self.assertLessEqual(similarity.value, 1.0)

    @given(compatible_vector_lists())
    def test_generated_graphs_preserve_invariants(self, vectors: list[list[float]]) -> None:
        graph = SemanticGraph(CacheConfig(edge_threshold=Similarity(0.0)))
        for index, values in enumerate(vectors):
            graph.add(f"query-{index}", f"response-{index}", tuple(values))
        graph.assert_invariants()

    def test_semantic_hit_does_not_call_llm_or_add_node(self) -> None:
        llm = CountingLlmService()
        cache = SemanticCache(DeterministicEmbeddingService(), llm)

        first = cache.request("How do I reset my password?")
        before_size = cache.graph.size
        second = cache.request("I forgot my password recovery process")

        self.assertFalse(first.cache_hit)
        self.assertTrue(second.cache_hit)
        self.assertEqual(second.response, first.response)
        self.assertEqual(llm.call_count, 1)
        self.assertEqual(cache.graph.size, before_size)


if __name__ == "__main__":
    unittest.main()
