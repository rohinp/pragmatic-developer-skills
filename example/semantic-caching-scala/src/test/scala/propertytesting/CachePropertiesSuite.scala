package propertytesting

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import semanticcache.*
import semanticcache.Similarity.*

class CachePropertiesSuite extends ScalaCheckSuite:
  private val positiveVector = for
    size <- Gen.choose(1, 6)
    values <- Gen.listOfN(size, Gen.choose(1, 100).map(_.toDouble))
  yield values.toVector

  private val compatibleVectors = for
    dimension <- Gen.choose(1, 6)
    count <- Gen.choose(1, 12)
    vectors <- Gen.listOfN(
      count,
      Gen.listOfN(dimension, Gen.choose(1, 100).map(_.toDouble)).map(_.toVector)
    )
  yield vectors

  property("cosine is reflexive and bounded") {
    forAll(positiveVector) { vector =>
      val similarity = CosineSimilarity.calculate(vector, vector).toOption.get
      assertEqualsDouble(similarity.value, 1.0, 1e-12)
      assert(similarity.value >= 0.0 && similarity.value <= 1.0)
    }
  }

  property("generated graphs preserve their invariants") {
    forAll(compatibleVectors) { vectors =>
      val graph = SemanticGraph(CacheConfig.create(edgeThreshold = 0.0).toOption.get)
      vectors.zipWithIndex.foreach { (vector, index) =>
        graph.add(s"query-$index", s"response-$index", vector).toOption.get
      }
      assertEquals(graph.invariantViolations, Vector.empty)
    }
  }

  test("a semantic hit does not call the LLM or add a node") {
    val llm = CountingLlmService()
    val cache = SemanticCache(DeterministicEmbeddingService(), llm)
    val first = cache.request("How do I reset my password?").toOption.get
    val beforeSize = cache.graph.size
    val second = cache.request("I forgot my password recovery process").toOption.get

    assert(!first.cacheHit)
    assert(second.cacheHit)
    assertEquals(second.response, first.response)
    assertEquals(llm.callCount, 1)
    assertEquals(cache.graph.size, beforeSize)
  }

