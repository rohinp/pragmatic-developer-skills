package invariants

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.SemanticGraph
import semanticcache.Similarity

class GraphInvariantsTest {
    @Test
    fun `edges are symmetric and traversal is bounded`() {
        val config = CacheConfig(
            edgeThreshold = Similarity(0.2),
            hitThreshold = Similarity(0.95),
            searchStartLimit = 2,
            neighborsPerStart = 1,
        )
        val graph = SemanticGraph(config)
        val first = graph.add("password", "a", listOf(1.0, 0.0, 0.0))
        val second = graph.add("password and billing", "b", listOf(1.0, 1.0, 0.0))
        graph.add("billing", "c", listOf(0.0, 1.0, 0.0))

        assertTrue(graph.invariantViolations().isEmpty())
        assertEquals(graph.neighbors(first.id)[second.id], graph.neighbors(second.id)[first.id])
        assertTrue(graph.find(listOf(1.0, 0.0, 0.0)).inspectedNodes <= 4)
    }
}

