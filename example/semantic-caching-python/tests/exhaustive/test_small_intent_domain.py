import itertools
import unittest

from semantic_cache import CountingLlmService, DeterministicEmbeddingService, SemanticCache


class SmallIntentDomainTest(unittest.TestCase):
    def test_every_short_intent_sequence_calls_llm_once_per_intent(self) -> None:
        queries = (
            "reset password",
            "billing invoice",
            "weather forecast",
        )

        for length in range(5):
            for sequence in itertools.product(queries, repeat=length):
                llm = CountingLlmService()
                cache = SemanticCache(DeterministicEmbeddingService(), llm)
                for query in sequence:
                    cache.request(query)

                self.assertEqual(
                    llm.call_count,
                    len(set(sequence)),
                    msg=f"failed for sequence {sequence}",
                )


if __name__ == "__main__":
    unittest.main()

