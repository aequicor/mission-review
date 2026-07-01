package com.aequicor.missionreview.core.uicontracts

import com.aequicor.missionreview.core.coderender.RenderedReviewFile

/**
 * Toolkit-neutral review screen model.
 *
 * @property projectRoot absolute project path, or null before project selection.
 * @property files rendered files available for review.
 * @property selectedFilePath selected repository-relative file path.
 * @property commentsCount number of collected comments.
 * @property canComplete whether completion actions should be enabled.
 */
data class ReviewScreenModel(
    val projectRoot: String?,
    val files: List<RenderedReviewFile>,
    val selectedFilePath: String?,
    val commentsCount: Int,
    val canComplete: Boolean,
)

/**
 * Toolkit-neutral user intents from the review screen.
 */
sealed interface ReviewScreenIntent {
    /**
     * Selects a project by absolute [path].
     */
    data class SelectProject(val path: String) : ReviewScreenIntent

    /**
     * Selects a file by repository-relative [path].
     */
    data class SelectFile(val path: String) : ReviewScreenIntent

    /**
     * Adds a review comment.
     */
    data class AddComment(
        val filePath: String,
        val line: Int?,
        val body: String,
        val required: Boolean,
    ) : ReviewScreenIntent

    /**
     * Copies exported comments to clipboard.
     */
    data object CopyCommentariesToClipboard : ReviewScreenIntent

    /**
     * Saves comments to the active MCP session.
     */
    data object SaveThroughMcp : ReviewScreenIntent
}
