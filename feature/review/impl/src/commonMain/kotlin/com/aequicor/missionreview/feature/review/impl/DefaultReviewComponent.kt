package com.aequicor.missionreview.feature.review.impl

import com.aequicor.missionreview.core.coderender.RenderedFileStatus
import com.aequicor.missionreview.core.coderender.RenderedLineType
import com.aequicor.missionreview.core.coderender.RenderedReviewFile
import com.aequicor.missionreview.core.coderender.toRenderedReviewFiles
import com.aequicor.missionreview.core.git.GitChangeReader
import com.aequicor.missionreview.core.git.GitRepository
import com.aequicor.missionreview.core.mcp.ReviewMcpGateway
import com.aequicor.missionreview.core.storage.StoredReviewComment
import com.aequicor.missionreview.feature.review.api.CodeNavigator
import com.aequicor.missionreview.feature.review.api.ReviewComment
import com.aequicor.missionreview.feature.review.api.ReviewComponent
import com.aequicor.missionreview.feature.review.api.ReviewFile
import com.aequicor.missionreview.feature.review.api.ReviewFileStatus
import com.aequicor.missionreview.feature.review.api.ReviewLine
import com.aequicor.missionreview.feature.review.api.ReviewLineType
import com.aequicor.missionreview.feature.review.api.ReviewState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default review component backed by Git changes, MCP session storage and target navigation.
 */
class DefaultReviewComponent(
    componentContext: ComponentContext,
    projectRoot: String,
    private val gitChangeReader: GitChangeReader,
    private val mcpGateway: ReviewMcpGateway,
    private val navigator: CodeNavigator = CodeNavigator { _, _ -> },
) : ReviewComponent,
    ComponentContext by componentContext {

    private val mutableState = MutableValue(ReviewState(projectRoot = projectRoot))
    private var nextCommentId = 1

    override val state: Value<ReviewState> = mutableState

    init {
        reload()
    }

    override fun reload() {
        runCatching {
            gitChangeReader
                .readChanges(GitRepository(mutableState.value.projectRoot))
                .toRenderedReviewFiles()
                .map(RenderedReviewFile::toReviewFile)
        }.onSuccess { files ->
            mutableState.value = mutableState.value.copy(
                files = files,
                selectedFilePath = files.firstOrNull()?.path,
                errorMessage = null,
            )
        }.onFailure { error ->
            mutableState.value = mutableState.value.copy(errorMessage = error.message)
        }
    }

    override fun selectFile(path: String) {
        mutableState.value = mutableState.value.copy(selectedFilePath = path)
    }

    override fun addComment(filePath: String, line: Int?, body: String, required: Boolean) {
        val trimmedBody = body.trim()
        if (trimmedBody.isEmpty()) return

        val comment = ReviewComment(
            id = "comment-${nextCommentId++}",
            filePath = filePath,
            line = line,
            body = trimmedBody,
            required = required,
        )

        mutableState.value = mutableState.value.copy(
            comments = mutableState.value.comments + comment,
            exportedCommentaries = null,
        )
    }

    override fun exportCommentaries(): String {
        val exported = ReviewCommentExporter.export(mutableState.value)
        mutableState.value = mutableState.value.copy(exportedCommentaries = exported)
        return exported
    }

    override fun saveThroughMcp(): String? {
        val current = mutableState.value
        val sessionId = current.activeMcpSessionId
            ?: mcpGateway.createReviewSession(current.projectRoot).sessionId

        val saved = mcpGateway.completeReviewSession(
            sessionId = sessionId,
            comments = current.comments.map(ReviewComment::toStoredReviewComment),
        )

        if (saved) {
            mutableState.value = current.copy(activeMcpSessionId = sessionId)
            return sessionId
        }

        mutableState.value = current.copy(errorMessage = "MCP review session was not found: $sessionId")
        return null
    }

    override fun completeWebReview(sessionId: String): Boolean =
        mcpGateway.completeReviewSession(
            sessionId = sessionId,
            comments = mutableState.value.comments.map(ReviewComment::toStoredReviewComment),
        )

    override fun openFile(filePath: String, line: Int?) {
        navigator.open(filePath, line)
    }
}

/**
 * Exports collected review comments into deterministic plain text.
 */
object ReviewCommentExporter {
    fun export(state: ReviewState): String =
        buildString {
            appendLine("Mission review commentaries")
            appendLine("Project: ${state.projectRoot}")
            appendLine("Scope: staged and untracked files")
            appendLine()

            if (state.comments.isEmpty()) {
                appendLine("No review commentaries.")
                return@buildString
            }

            state.comments
                .sortedWith(
                    compareBy<ReviewComment> { it.filePath }
                        .thenBy { it.line ?: Int.MAX_VALUE }
                        .thenBy { it.id },
                )
                .forEachIndexed { index, comment ->
                    appendLine("${index + 1}. ${if (comment.required) "Required" else "Comment"}")
                    appendLine("File: ${comment.filePath}")
                    comment.line?.let { appendLine("Line: $it") }
                    appendLine(comment.body)
                    appendLine()
                }
        }.trimEnd()
}

private fun RenderedReviewFile.toReviewFile(): ReviewFile =
    ReviewFile(
        path = path,
        status = when (status) {
            RenderedFileStatus.STAGED -> ReviewFileStatus.STAGED
            RenderedFileStatus.UNTRACKED -> ReviewFileStatus.UNTRACKED
        },
        lines = lines.map { line ->
            ReviewLine(
                number = line.number,
                type = when (line.type) {
                    RenderedLineType.CONTEXT -> ReviewLineType.CONTEXT
                    RenderedLineType.ADDITION -> ReviewLineType.ADDITION
                    RenderedLineType.DELETION -> ReviewLineType.DELETION
                    RenderedLineType.METADATA -> ReviewLineType.METADATA
                },
                text = line.text,
            )
        },
    )

private fun ReviewComment.toStoredReviewComment(): StoredReviewComment =
    StoredReviewComment(
        filePath = filePath,
        line = line,
        body = body,
        required = required,
    )
