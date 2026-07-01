package com.aequicor.missionreview.core.mcp

import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.ReviewSessionStore
import com.aequicor.missionreview.core.storage.StoredReviewComment
import com.aequicor.missionreview.core.storage.StoredReviewSessionStatus

/**
 * MCP-visible review session metadata.
 *
 * @property sessionId stable local session id.
 * @property projectRoot absolute project path under review.
 * @property reviewLink local URL opened by the human reviewer.
 */
data class McpReviewSession(
    val sessionId: String,
    val projectRoot: String,
    val reviewLink: String,
)

/**
 * Completed review result returned through MCP.
 *
 * @property sessionId completed session id.
 * @property comments review comments saved by the reviewer.
 */
data class McpReviewResult(
    val sessionId: String,
    val comments: List<StoredReviewComment>,
)

/**
 * Boundary for MCP-driven review session lifecycle.
 */
interface ReviewMcpGateway {
    /**
     * Creates a new review session for [projectRoot].
     */
    fun createReviewSession(projectRoot: String): McpReviewSession

    /**
     * Completes [sessionId] with reviewer [comments].
     */
    fun completeReviewSession(sessionId: String, comments: List<StoredReviewComment>): Boolean

    /**
     * Reads a completed review session, or null when it is missing or still open.
     */
    fun readCompletedReview(sessionId: String): McpReviewResult?
}

/**
 * In-process MCP gateway implementation backed by a [ReviewSessionStore].
 */
class LocalReviewMcpGateway(
    private val store: ReviewSessionStore,
    private val linkBuilder: LocalReviewLinkBuilder,
) : ReviewMcpGateway {
    override fun createReviewSession(projectRoot: String): McpReviewSession {
        val session = store.create(projectRoot)
        return McpReviewSession(
            sessionId = session.id,
            projectRoot = projectRoot,
            reviewLink = linkBuilder.linkFor(session.id),
        )
    }

    override fun completeReviewSession(sessionId: String, comments: List<StoredReviewComment>): Boolean =
        store.complete(sessionId, comments) != null

    override fun readCompletedReview(sessionId: String): McpReviewResult? {
        val session = store.find(sessionId) ?: return null
        if (session.status != StoredReviewSessionStatus.COMPLETED) return null
        return McpReviewResult(
            sessionId = session.id,
            comments = session.comments,
        )
    }
}
