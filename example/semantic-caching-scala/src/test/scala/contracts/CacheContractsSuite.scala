package contracts

import munit.FunSuite
import semanticcache.*

class CacheContractsSuite extends FunSuite:
  test("cosine requires compatible non-zero embeddings") {
    assert(CosineSimilarity.calculate(Vector.empty, Vector.empty).isLeft)
    assert(CosineSimilarity.calculate(Vector(1.0), Vector(1.0, 0.0)).isLeft)
    assert(CosineSimilarity.calculate(Vector(0.0, 0.0), Vector(1.0, 0.0)).isLeft)
  }

  test("hit threshold cannot be lower than edge threshold") {
    assert(CacheConfig.create(edgeThreshold = 0.8, hitThreshold = 0.7).isLeft)
  }

  test("a rejected incompatible embedding leaves the graph unchanged") {
    val graph = SemanticGraph()
    assert(graph.add("password", "response", Vector(1.0, 0.0)).isRight)
    assert(graph.add("billing", "response", Vector(0.0, 1.0, 0.0)).isLeft)
    assertEquals(graph.size, 1)
    assertEquals(graph.invariantViolations, Vector.empty)
  }

