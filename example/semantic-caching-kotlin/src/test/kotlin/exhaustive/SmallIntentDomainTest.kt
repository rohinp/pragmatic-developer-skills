package exhaustive

import kotlin.test.Test
import kotlin.test.assertEquals
import semanticcache.CountingLlmService
import semanticcache.DeterministicEmbeddingService
import semanticcache.SemanticCache

class SmallIntentDomainTest {
    private val queries = listOf("reset password", "billing invoice", "weather forecast")

    private fun sequences(length: Int): List<List<String>> =
        if (length == 0) {
            listOf(emptyList())
        } else {
            queries.flatMap { head -> sequences(length - 1).map { tail -> listOf(head) + tail } }
        }

    @Test
    fun `every short intent sequence calls the LLM once per intent`() {
        (0..4).flatMap(::sequences).forEach { sequence ->
            val llm = CountingLlmService()
            val cache = SemanticCache(DeterministicEmbeddingService(), llm)
            sequence.forEach(cache::request)
            assertEquals(sequence.distinct().size, llm.callCount, "failed for sequence $sequence")
        }
    }
}

