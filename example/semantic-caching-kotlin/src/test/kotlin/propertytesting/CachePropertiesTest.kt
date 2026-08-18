package propertytesting

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.CountingLlmService
import semanticcache.DeterministicEmbeddingService
import semanticcache.SemanticCache
import semanticcache.SemanticGraph
import semanticcache.Similarity
import semanticcache.cosineSimilarity

class CachePropertiesTest : FunSpec({
    test("cosine is reflexive and bounded") {
        checkAll(Arb.list(Arb.int(1..100), 1..6)) { values ->
            val vector = values.map(Int::toDouble)
            val similarity = cosineSimilarity(vector, vector)
            assertEquals(1.0, similarity.value, 1e-12)
            assertTrue(similarity.value in 0.0..1.0)
        }
    }

    test("generated graphs preserve their invariants") {
        val compatibleVectors = Arb.int(1..6).flatMap { dimension ->
            Arb.list(
                Arb.list(Arb.int(1..100), dimension..dimension),
                1..12,
            )
        }

        checkAll(compatibleVectors) { vectors ->
            val graph = SemanticGraph(CacheConfig(edgeThreshold = Similarity(0.0)))
            vectors.forEachIndexed { index, values ->
                graph.add("query-$index", "response-$index", values.map(Int::toDouble))
            }
            assertTrue(graph.invariantViolations().isEmpty())
        }
    }

    test("a semantic hit does not call the LLM or add a node") {
        val llm = CountingLlmService()
        val cache = SemanticCache(DeterministicEmbeddingService(), llm)
        val first = cache.request("How do I reset my password?")
        val beforeSize = cache.graph.size
        val second = cache.request("I forgot my password recovery process")

        assertFalse(first.cacheHit)
        assertTrue(second.cacheHit)
        assertEquals(first.response, second.response)
        assertEquals(1, llm.callCount)
        assertEquals(beforeSize, cache.graph.size)
    }
})

