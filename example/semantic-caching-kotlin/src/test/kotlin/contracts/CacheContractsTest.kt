package contracts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.SemanticGraph
import semanticcache.Similarity
import semanticcache.cosineSimilarity

class CacheContractsTest {
    @Test
    fun `cosine requires compatible non-zero embeddings`() {
        assertFailsWith<IllegalArgumentException> { cosineSimilarity(emptyList(), emptyList()) }
        assertFailsWith<IllegalArgumentException> { cosineSimilarity(listOf(1.0), listOf(1.0, 0.0)) }
        assertFailsWith<IllegalArgumentException> {
            cosineSimilarity(listOf(0.0, 0.0), listOf(1.0, 0.0))
        }
    }

    @Test
    fun `hit threshold cannot be lower than edge threshold`() {
        assertFailsWith<IllegalArgumentException> {
            CacheConfig(edgeThreshold = Similarity(0.8), hitThreshold = Similarity(0.7))
        }
    }

    @Test
    fun `a rejected incompatible embedding leaves the graph unchanged`() {
        val graph = SemanticGraph()
        graph.add("password", "response", listOf(1.0, 0.0))

        assertFailsWith<IllegalArgumentException> {
            graph.add("billing", "response", listOf(0.0, 1.0, 0.0))
        }

        assertEquals(1, graph.size)
        assertTrue(graph.invariantViolations().isEmpty())
    }
}

