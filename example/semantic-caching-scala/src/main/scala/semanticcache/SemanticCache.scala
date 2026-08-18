package semanticcache

import scala.collection.mutable

type Embedding = Vector[Double]

opaque type QueryId = Int

object QueryId:
  private[semanticcache] def next(value: Int): QueryId = value

  extension (id: QueryId) def value: Int = id

opaque type Similarity = Double

object Similarity:
  def from(value: Double): Either[String, Similarity] =
    Either.cond(
      value >= 0.0 && value <= 1.0,
      value,
      "similarity must be between zero and one"
    )

  private[semanticcache] def trusted(value: Double): Similarity = value

  extension (similarity: Similarity)
    def value: Double = similarity
    def >=(other: Similarity): Boolean = similarity >= other
    def <(other: Similarity): Boolean = similarity < other

final case class CacheConfig private (
    edgeThreshold: Similarity,
    hitThreshold: Similarity,
    candidateLimit: Int,
    searchStartLimit: Int,
    neighborsPerStart: Int
)

object CacheConfig:
  def create(
      edgeThreshold: Double = 0.20,
      hitThreshold: Double = 0.90,
      candidateLimit: Int = 10,
      searchStartLimit: Int = 3,
      neighborsPerStart: Int = 2
  ): Either[String, CacheConfig] =
    for
      edge <- Similarity.from(edgeThreshold)
      hit <- Similarity.from(hitThreshold)
      _ <- Either.cond(hit >= edge, (), "hit threshold must be at least the edge threshold")
      _ <- Either.cond(
        List(candidateLimit, searchStartLimit, neighborsPerStart).forall(_ > 0),
        (),
        "all traversal limits must be positive"
      )
    yield CacheConfig(edge, hit, candidateLimit, searchStartLimit, neighborsPerStart)

  val default: CacheConfig = create().fold(message => throw IllegalStateException(message), identity)

final case class CacheNode(id: QueryId, query: String, response: String, embedding: Embedding)

final case class SearchResult(
    node: Option[CacheNode],
    similarity: Option[Similarity],
    inspectedNodes: Int
)

final case class CacheOutcome(response: String, cacheHit: Boolean, inspectedNodes: Int)

trait EmbeddingService:
  def embed(query: String): Embedding

trait LlmService:
  def generate(query: String): String

object CosineSimilarity:
  def calculate(left: Embedding, right: Embedding): Either[String, Similarity] =
    if left.isEmpty || left.size != right.size then
      Left("embeddings must be non-empty and have equal dimensions")
    else if (left ++ right).exists(_ < 0.0) then
      Left("this example models non-negative embeddings only")
    else
      val leftNorm = math.sqrt(left.map(value => value * value).sum)
      val rightNorm = math.sqrt(right.map(value => value * value).sum)
      if leftNorm == 0.0 || rightNorm == 0.0 then Left("embeddings must have non-zero magnitude")
      else
        val raw = left.zip(right).map(_ * _).sum / (leftNorm * rightNorm)
        Right(Similarity.trusted(math.min(1.0, math.max(0.0, raw))))

final class SemanticGraph(config: CacheConfig = CacheConfig.default):
  import QueryId.*
  import Similarity.*

  private val nodes = mutable.LinkedHashMap.empty[QueryId, CacheNode]
  private val edges = mutable.LinkedHashMap.empty[QueryId, mutable.LinkedHashMap[QueryId, Similarity]]
  private val recent = mutable.ArrayBuffer.empty[QueryId]
  private var nextId = 1

  def size: Int = nodes.size

  def add(query: String, response: String, embedding: Embedding): Either[String, CacheNode] =
    if query.trim.isEmpty then Left("query must not be blank")
    else
      val candidateIds = recent.take(config.candidateLimit).toVector
      val candidateScores = candidateIds.foldLeft(
        Right(Vector.empty[(QueryId, Similarity)]): Either[String, Vector[(QueryId, Similarity)]]
      ) { (result, candidateId) =>
        for
          scores <- result
          similarity <- CosineSimilarity.calculate(embedding, nodes(candidateId).embedding)
        yield scores :+ (candidateId -> similarity)
      }

      for
        _ <- CosineSimilarity.calculate(embedding, embedding)
        scores <- candidateScores
      yield
        val nodeId = QueryId.next(nextId)
        nextId += 1
        val node = CacheNode(nodeId, query, response, embedding)
        nodes(nodeId) = node
        edges(nodeId) = mutable.LinkedHashMap.empty

        scores.foreach { (candidateId, similarity) =>
          if similarity >= config.edgeThreshold then
            edges(nodeId)(candidateId) = similarity
            edges(candidateId)(nodeId) = similarity
        }

        recent.prepend(nodeId)
        require(invariantViolations.isEmpty, invariantViolations.mkString("; "))
        node

  def find(embedding: Embedding): Either[String, SearchResult] =
    CosineSimilarity.calculate(embedding, embedding).flatMap { _ =>
      val candidateIds = mutable.LinkedHashSet.empty[QueryId]
      recent.take(config.searchStartLimit).foreach { startId =>
        candidateIds += startId
        edges(startId).toVector
          .sortBy { (id, similarity) => (-similarity.value, id.value) }
          .take(config.neighborsPerStart)
          .foreach { (neighborId, _) => candidateIds += neighborId }
      }

      candidateIds.toVector
        .foldLeft(Right(Vector.empty[(Similarity, QueryId)]): Either[String, Vector[(Similarity, QueryId)]]) {
          (result, nodeId) =>
            for
              scored <- result
              similarity <- CosineSimilarity.calculate(embedding, nodes(nodeId).embedding)
            yield scored :+ (similarity -> nodeId)
        }
        .map { scored =>
          val eligible = scored.filter((similarity, _) => similarity >= config.hitThreshold)
          eligible.maxByOption((similarity, id) => (similarity.value, -id.value)) match
            case Some((similarity, id)) =>
              SearchResult(Some(nodes(id)), Some(similarity), candidateIds.size)
            case None => SearchResult(None, None, candidateIds.size)
        }
    }

  def neighbors(nodeId: QueryId): Map[QueryId, Similarity] = edges(nodeId).toMap

  def invariantViolations: Vector[String] =
    val violations = Vector.newBuilder[String]
    if nodes.keySet != edges.keySet then violations += "every node must own an adjacency map"
    if recent.distinct.size != recent.size || recent.toSet != nodes.keySet.toSet then
      violations += "recent nodes must contain every node exactly once"

    edges.foreach { (source, neighbors) =>
      neighbors.foreach { (target, weight) =>
        if source == target then violations += "self-loops are forbidden"
        if !nodes.contains(target) then violations += "edge target must exist"
        if weight < config.edgeThreshold then violations += "edge weight must satisfy the threshold"
        if !edges.get(target).exists(_.get(source).contains(weight)) then
          violations += "edges must be symmetric with equal weights"
      }
    }
    violations.result()

final class SemanticCache(
    embeddings: EmbeddingService,
    llm: LlmService,
    config: CacheConfig = CacheConfig.default
):
  val graph = SemanticGraph(config)

  def request(query: String): Either[String, CacheOutcome] =
    if query.trim.isEmpty then Left("query must not be blank")
    else
      val embedding = embeddings.embed(query)
      graph.find(embedding).flatMap { cached =>
        cached.node match
          case Some(node) => Right(CacheOutcome(node.response, cacheHit = true, cached.inspectedNodes))
          case None =>
            val response = llm.generate(query)
            graph.add(query, response, embedding).map(_ =>
              CacheOutcome(response, cacheHit = false, cached.inspectedNodes)
            )
      }

final class DeterministicEmbeddingService extends EmbeddingService:
  private val intents = Vector(
    Set("password", "reset", "recovery", "forgot"),
    Set("invoice", "billing", "payment", "receipt"),
    Set("weather", "rain", "temperature", "forecast")
  )

  def embed(query: String): Embedding =
    val words = query.split("\\s+").map(_.replaceAll("[?.,!]", "").toLowerCase).toSet
    val vector = intents.map(vocabulary => words.count(vocabulary.contains).toDouble)
    if vector.exists(_ > 0.0) then vector else Vector(1.0, 1.0, 1.0)

final class CountingLlmService extends LlmService:
  private var calls = 0

  def callCount: Int = calls

  def generate(query: String): String =
    calls += 1
    s"mock-response:$query"

