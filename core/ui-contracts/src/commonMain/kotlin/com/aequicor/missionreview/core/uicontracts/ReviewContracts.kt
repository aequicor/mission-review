package com.aequicor.missionreview.core.uicontracts

import com.aequicor.missionreview.core.coderender.RenderedReviewFile

data class ReviewScreenModel(
    val projectRoot: String?,
    val files: List<RenderedReviewFile>,
    val selectedFilePath: String?,
    val commentsCount: Int,
    val canComplete: Boolean,
)

sealed interface ReviewScreenIntent {
    data class SelectProject(val path: String) : ReviewScreenIntent
    data class SelectFile(val path: String) : ReviewScreenIntent
    data class AddComment(val filePath: String, val line: Int?, val body: String, val required: Boolean) : ReviewScreenIntent
    data object CopyCommentariesToClipboard : ReviewScreenIntent
    data object SaveThroughMcp : ReviewScreenIntent
}
