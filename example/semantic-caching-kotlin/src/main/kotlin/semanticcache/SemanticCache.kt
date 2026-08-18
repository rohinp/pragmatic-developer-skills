package semanticcache

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

typealias Embedding = List<Double>

@JvmInline
value class QueryId internal constructor(val value: Int) {
    init {
        require(value > 0) { "query id must be positive" }
    }
}

@JvmInline
value class Similarity(val value: Double) : Comparable<Similarity> {
    init {
        require(value in 0.0..1.0) { "similarity must be between zero and one" }
    }

    override fun compareTo(other: Similarity): Int = value.compareTo(other.value)
}

data class CacheConfig(
    val edgeThreshold: Similarity = Similarity(0.20),
    val hitThreshold: Similarity = Similarity(0.90),
    val candidateLimit: Int = 10,
    val searchStartLimit: Int = 3,
    val neighborsPerStart: Int = 2,
) {
    init {
        require(hitThreshold >= edgeThreshold) {
            "hit threshold must be at least the edge threshold"
        }
        require(listOf(candidateLimit, searchStartLimit, neighborsPerStart).all { it > 0 }) {
            "all traversal limits must be positive"
        }
    }
}

data class CacheNode(
    val id: QueryId,
    val query: String,
    val response: String,
    val embedding: Embedding,
)

data class SearchResult(
    val node: CacheNode?,
    val similarity: Similarity?,
    val inspectedNodes: Int,
)

data class CacheOutcome(
    val response: String,
    val cacheHit: Boolean,
    val inspectedNodes: Int,
)

fun interface EmbeddingService {
    fun embed(query: String): Embedding
}

fun interface LlmService {
    fun generate(query: String): String
}

fun cosineSimilarity(left: Embedding, right: Embedding): Similarity {
    require(left.isNotEmpty() && left.size == right.size) {
        "embeddings must be non-empty and have equal dimensions"
    }
    require((left + right).none { it < 0.0 }) {
        "this example models non-negative embeddings only"
    }

    val leftNorm = sqrt(left.sumOf { it * it })
    val rightNorm = sqrt(right.sumOf { it * it })
    require(leftNorm != 0.0 && rightNorm != 0.0) {
        "embeddings must have non-zero magnitude"
    }

    val raw = left.zip(right).sumOf { (a, b) -> a * b } / (leftNorm * rightNorm)
    return Similarity(min(1.0, max(0.0, raw)))
}

class SemanticGraph(private val config: CacheConfig = CacheConfig()) {
    private val nodes = linkedMapOf<QueryId, CacheNode>()
    private val edges = linkedMapOf<QueryId, LinkedHashMap<QueryId, Similarity>>()
    private val recent = mutableListOf<QueryId>()
    private var nextId = 1

    val size: Int
        get() = nodes.size

    fun add(query: String, response: String, embedding: Embedding): CacheNode {
        require(query.isNotBlank()) { "query must not be blank" }
        cosineSimilarity(embedding, embedding)

        val candidateScores = recent.take(config.candidateLimit).map { candidateId ->
            candidateId to cosineSimilarity(embedding, checkNotNull(nodes[candidateId]).embedding)
        }

        val nodeId = QueryId(nextId++)
        val node = CacheNode(nodeId, query, response, embedding)
        nodes[nodeId] = node
        edges[nodeId] = linkedMapOf()

        candidateScores.forEach { (candidateId, similarity) ->
            if (similarity >= config.edgeThreshold) {
                checkNotNull(edges[nodeId])[candidateId] = similarity
                checkNotNull(edges[candidateId])[nodeId] = similarity
            }
        }

        recent.add(0, nodeId)
        check(invariantViolations().isEmpty()) { invariantViolations().joinToString("; ") }
        return node
    }

    fun find(embedding: Embedding): SearchResult {
        cosineSimilarity(embedding, embedding)
        val candidateIds = linkedSetOf<QueryId>()

        recent.take(config.searchStartLimit).forEach { startId ->
            candidateIds += startId
            checkNotNull(edges[startId])
                .entries
                .sortedWith(compareByDescending<Map.Entry<QueryId, Similarity>> { it.value.value }.thenBy { it.key.value })
                .take(config.neighborsPerStart)
                .forEach { candidateIds += it.key }
        }

        val best = candidateIds
            .map { nodeId -> cosineSimilarity(embedding, checkNotNull(nodes[nodeId]).embedding) to nodeId }
            .filter { (similarity, _) -> similarity >= config.hitThreshold }
            .maxWithOrNull(compareBy<Pair<Similarity, QueryId>> { it.first.value }.thenBy { -it.second.value })

        return if (best == null) {
            SearchResult(null, null, candidateIds.size)
        } else {
            SearchResult(checkNotNull(nodes[best.second]), best.first, candidateIds.size)
        }
    }

    fun neighbors(nodeId: QueryId): Map<QueryId, Similarity> = checkNotNull(edges[nodeId]).toMap()

    fun invariantViolations(): List<String> = buildList {
        if (nodes.keys != edges.keys) add("every node must own an adjacency map")
        if (recent.distinct().size != recent.size || recent.toSet() != nodes.keys.toSet()) {
            add("recent nodes must contain every node exactly once")
        }

        edges.forEach { (source, neighbors) ->
            neighbors.forEach { (target, weight) ->
                if (source == target) add("self-loops are forbidden")
                if (target !in nodes) add("edge target must exist")
                if (weight < config.edgeThreshold) add("edge weight must satisfy the threshold")
                if (edges[target]?.get(source) != weight) {
                    add("edges must be symmetric with equal weights")
                }
            }
        }
    }
}

class SemanticCache(
    private val embeddings: EmbeddingService,
    private val llm: LlmService,
    config: CacheConfig = CacheConfig(),
) {
    val graph = SemanticGraph(config)

    fun request(query: String): CacheOutcome {
        require(query.isNotBlank()) { "query must not be blank" }
        val embedding = embeddings.embed(query)
        val cached = graph.find(embedding)
        val cachedNode = cached.node

        if (cachedNode != null) {
            return CacheOutcome(cachedNode.response, cacheHit = true, cached.inspectedNodes)
        }

        val response = llm.generate(query)
        graph.add(query, response, embedding)
        return CacheOutcome(response, cacheHit = false, cached.inspectedNodes)
    }
}

class DeterministicEmbeddingService : EmbeddingService {
    private val intents = listOf(
        setOf("password", "reset", "recovery", "forgot"),
        setOf("invoice", "billing", "payment", "receipt"),
        setOf("weather", "rain", "temperature", "forecast"),
    )

    override fun embed(query: String): Embedding {
        val words = query
            .split(Regex("\\s+"))
            .map { it.replace(Regex("[?.,!]"), "").lowercase() }
            .toSet()
        val vector = intents.map { vocabulary -> words.count(vocabulary::contains).toDouble() }
        return if (vector.any { it > 0.0 }) vector else listOf(1.0, 1.0, 1.0)
    }
}

class CountingLlmService : LlmService {
    var callCount: Int = 0
        private set

    override fun generate(query: String): String {
        callCount += 1
        return "mock-response:$query"
    }
}

