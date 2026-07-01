package com.aequicor.missionreview.core.mcp

import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.ReviewSessionStore
import com.aequicor.missionreview.core.storage.StoredReviewComment
import com.aequicor.missionreview.core.storage.StoredReviewSessionStatus

data class McpReviewSession(
    val sessionId: String,
    val projectRoot: String,
    val reviewLink: String,
)

data class McpReviewResult(
    val sessionId: String,
    val comments: List<StoredReviewComment>,
)

interface ReviewMcpGateway {
    fun createReviewSession(projectRoot: String): McpReviewSession
    fun completeReviewSession(sessionId: String, comments: List<StoredReviewComment>): Boolean
    fun readCompletedReview(sessionId: String): McpReviewResult?
}

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
