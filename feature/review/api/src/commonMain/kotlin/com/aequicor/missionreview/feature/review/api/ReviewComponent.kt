package com.aequicor.missionreview.feature.review.api

import com.arkivanov.decompose.value.Value

/**
 * Review file source category.
 */
enum class ReviewFileStatus {
    STAGED,
    UNTRACKED,
}

/**
 * Rendered code line category used by review UI adapters.
 */
enum class ReviewLineType {
    CONTEXT,
    ADDITION,
    DELETION,
    METADATA,
}

/**
 * A single rendered code or diff line.
 *
 * @property number one-based file line number when known.
 * @property type visual and semantic line type.
 * @property text rendered line text.
 */
data class ReviewLine(
    val number: Int?,
    val type: ReviewLineType,
    val text: String,
)

/**
 * File shown in the review flow.
 *
 * @property path repository-relative file path.
 * @property status source status for the file.
 * @property lines rendered lines shown to the reviewer.
 */
data class ReviewFile(
    val path: String,
    val status: ReviewFileStatus,
    val lines: List<ReviewLine>,
)

/**
 * Reviewer comment attached to a file and optional line.
 *
 * @property id stable comment identifier inside the active review state.
 * @property filePath repository-relative file path.
 * @property line one-based target line, or null for a file-level comment.
 * @property body human-written review text.
 * @property required whether the comment requires a code change before commit.
 */
data class ReviewComment(
    val id: String,
    val filePath: String,
    val line: Int?,
    val body: String,
    val required: Boolean,
)

/**
 * State of an active review session.
 *
 * @property projectRoot absolute path to the reviewed project.
 * @property files files available for review.
 * @property selectedFilePath currently selected repository-relative file path.
 * @property comments collected review comments.
 * @property exportedCommentaries last deterministic export text.
 * @property activeMcpSessionId MCP session id when the flow was opened through MCP.
 * @property errorMessage last user-visible error.
 */
data class ReviewState(
    val projectRoot: String,
    val files: List<ReviewFile> = emptyList(),
    val selectedFilePath: String? = null,
    val comments: List<ReviewComment> = emptyList(),
    val exportedCommentaries: String? = null,
    val activeMcpSessionId: String? = null,
    val errorMessage: String? = null,
) {
    /**
     * Whether the review has comments that can be exported or saved.
     */
    val canComplete: Boolean
        get() = comments.isNotEmpty()
}

/**
 * Target-specific code navigation callback.
 */
fun interface CodeNavigator {
    /**
     * Opens [filePath] at [line] when a concrete line is available.
     */
    fun open(filePath: String, line: Int?)
}

/**
 * Public component contract for the local review flow.
 */
interface ReviewComponent {
    /**
     * Observable review state.
     */
    val state: Value<ReviewState>

    /**
     * Reloads review files from the project Git state.
     */
    fun reload()

    /**
     * Selects a file by repository-relative [path].
     */
    fun selectFile(path: String)

    /**
     * Adds a reviewer comment.
     */
    fun addComment(
        filePath: String,
        line: Int?,
        body: String,
        required: Boolean,
    )

    /**
     * Exports comments as deterministic text for an AI-agent prompt.
     */
    fun exportCommentaries(): String

    /**
     * Saves comments to the active MCP session.
     *
     * @return session id when saving succeeded.
     */
    fun saveThroughMcp(): String?

    /**
     * Completes a web review session with current comments.
     */
    fun completeWebReview(sessionId: String): Boolean

    /**
     * Requests target-specific navigation to [filePath].
     */
    fun openFile(filePath: String, line: Int?)
}
