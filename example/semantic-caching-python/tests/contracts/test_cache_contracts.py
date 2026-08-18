import unittest

from semantic_cache import CacheConfig, SemanticGraph, Similarity, cosine_similarity


class CacheContractsTest(unittest.TestCase):
    def test_cosine_requires_compatible_non_zero_embeddings(self) -> None:
        with self.assertRaises(ValueError):
            cosine_similarity((), ())
        with self.assertRaises(ValueError):
            cosine_similarity((1.0,), (1.0, 0.0))
        with self.assertRaises(ValueError):
            cosine_similarity((0.0, 0.0), (1.0, 0.0))

    def test_hit_threshold_cannot_be_lower_than_edge_threshold(self) -> None:
        with self.assertRaises(ValueError):
            CacheConfig(edge_threshold=Similarity(0.8), hit_threshold=Similarity(0.7))

    def test_query_must_not_be_blank(self) -> None:
        with self.assertRaises(ValueError):
            SemanticGraph().add(" ", "response", (1.0, 0.0, 0.0))

    def test_rejected_incompatible_embedding_leaves_graph_unchanged(self) -> None:
        graph = SemanticGraph()
        graph.add("password", "response", (1.0, 0.0))

        with self.assertRaises(ValueError):
            graph.add("billing", "response", (0.0, 1.0, 0.0))

        self.assertEqual(graph.size, 1)
        graph.assert_invariants()


if __name__ == "__main__":
    unittest.main()
