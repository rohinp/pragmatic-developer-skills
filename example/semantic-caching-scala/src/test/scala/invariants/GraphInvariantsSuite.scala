package invariants

import munit.FunSuite
import semanticcache.*

class GraphInvariantsSuite extends FunSuite:
  test("edges are symmetric and traversal is bounded") {
    val config = CacheConfig
      .create(edgeThreshold = 0.2, hitThreshold = 0.95, searchStartLimit = 2, neighborsPerStart = 1)
      .toOption
      .get
    val graph = SemanticGraph(config)
    val first = graph.add("password", "a", Vector(1.0, 0.0, 0.0)).toOption.get
    val second = graph.add("password and billing", "b", Vector(1.0, 1.0, 0.0)).toOption.get
    graph.add("billing", "c", Vector(0.0, 1.0, 0.0)).toOption.get

    assertEquals(graph.invariantViolations, Vector.empty)
    assertEquals(graph.neighbors(first.id)(second.id), graph.neighbors(second.id)(first.id))
    assert(graph.find(Vector(1.0, 0.0, 0.0)).toOption.get.inspectedNodes <= 4)
  }

