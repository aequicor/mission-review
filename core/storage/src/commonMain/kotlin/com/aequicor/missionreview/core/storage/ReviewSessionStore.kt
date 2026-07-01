package com.aequicor.missionreview.core.storage

enum class StoredReviewSessionStatus {
    OPEN,
    COMPLETED,
}

data class StoredReviewComment(
    val filePath: String,
    val line: Int?,
    val body: String,
    val required: Boolean,
)

data class StoredReviewSession(
    val id: String,
    val projectRoot: String,
    val status: StoredReviewSessionStatus,
    val comments: List<StoredReviewComment> = emptyList(),
)

interface ReviewSessionStore {
    fun create(projectRoot: String): StoredReviewSession
    fun complete(sessionId: String, comments: List<StoredReviewComment>): StoredReviewSession?
    fun find(sessionId: String): StoredReviewSession?
}

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
