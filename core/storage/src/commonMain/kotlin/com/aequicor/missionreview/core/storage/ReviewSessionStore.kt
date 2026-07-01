package com.aequicor.missionreview.core.storage

/**
 * Stored review session lifecycle status.
 */
enum class StoredReviewSessionStatus {
    OPEN,
    COMPLETED,
}

/**
 * Review comment persisted for MCP/web flows.
 *
 * @property filePath repository-relative file path.
 * @property line one-based target line, or null for a file-level comment.
 * @property body human-written review text.
 * @property required whether this comment requires a code change.
 */
data class StoredReviewComment(
    val filePath: String,
    val line: Int?,
    val body: String,
    val required: Boolean,
)

/**
 * Stored local review session.
 *
 * @property id stable local session id.
 * @property projectRoot absolute project path under review.
 * @property status current session status.
 * @property comments comments saved when the session is completed.
 */
data class StoredReviewSession(
    val id: String,
    val projectRoot: String,
    val status: StoredReviewSessionStatus,
    val comments: List<StoredReviewComment> = emptyList(),
)

/**
 * Storage boundary for local review sessions.
 */
interface ReviewSessionStore {
    /**
     * Creates an open review session for [projectRoot].
     */
    fun create(projectRoot: String): StoredReviewSession

    /**
     * Completes [sessionId] with [comments].
     */
    fun complete(sessionId: String, comments: List<StoredReviewComment>): StoredReviewSession?

    /**
     * Finds a session by [sessionId].
     */
    fun find(sessionId: String): StoredReviewSession?
}

/**
 * Volatile in-memory review session store for the initial skeleton.
 */
class InMemoryReviewSessionStore : ReviewSessionStore {
    private val sessions = mutableMapOf<String, StoredReviewSession>()
    private var nextId = 1

    override fun create(projectRoot: String): StoredReviewSession {
        val session = StoredReviewSession(
            id = "review-${nextId++}",
            projectRoot = projectRoot,
            status = StoredReviewSessionStatus.OPEN,
        )
        sessions[session.id] = session
        return session
    }

    override fun complete(sessionId: String, comments: List<StoredReviewComment>): StoredReviewSession? {
        val existing = sessions[sessionId] ?: return null
        val completed = existing.copy(
            status = StoredReviewSessionStatus.COMPLETED,
            comments = comments,
        )
        sessions[sessionId] = completed
        return completed
    }

    override fun find(sessionId: String): StoredReviewSession? =
        sessions[sessionId]
}
