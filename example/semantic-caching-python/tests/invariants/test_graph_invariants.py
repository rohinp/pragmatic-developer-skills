import unittest

from semantic_cache import CacheConfig, SemanticGraph, Similarity


class GraphInvariantsTest(unittest.TestCase):
    def test_edges_are_symmetric_and_traversal_is_bounded(self) -> None:
        config = CacheConfig(
            edge_threshold=Similarity(0.2),
            hit_threshold=Similarity(0.95),
            search_start_limit=2,
            neighbors_per_start=1,
        )
        graph = SemanticGraph(config)
        first = graph.add("password", "a", (1.0, 0.0, 0.0))
        second = graph.add("password and billing", "b", (1.0, 1.0, 0.0))
        graph.add("billing", "c", (0.0, 1.0, 0.0))

        graph.assert_invariants()
        self.assertEqual(graph.neighbors(first.id)[second.id], graph.neighbors(second.id)[first.id])
        self.assertLessEqual(graph.find((1.0, 0.0, 0.0)).inspected_nodes, 4)


if __name__ == "__main__":
    unittest.main()

