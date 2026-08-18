package exhaustive

import munit.FunSuite
import semanticcache.*

class SmallIntentDomainSuite extends FunSuite:
  private val queries = List("reset password", "billing invoice", "weather forecast")

  private def sequences(length: Int): List[List[String]] =
    if length == 0 then List(Nil)
    else for
      head <- queries
      tail <- sequences(length - 1)
    yield head :: tail

  test("every short intent sequence calls the LLM once per intent") {
    (0 to 4).flatMap(sequences).foreach { sequence =>
      val llm = CountingLlmService()
      val cache = SemanticCache(DeterministicEmbeddingService(), llm)
      sequence.foreach(query => cache.request(query).toOption.get)
      assertEquals(llm.callCount, sequence.distinct.size, clues(sequence))
    }
  }

