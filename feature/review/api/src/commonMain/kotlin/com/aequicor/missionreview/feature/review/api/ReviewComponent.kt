package com.aequicor.missionreview.feature.review.api

import com.arkivanov.decompose.value.Value

enum class ReviewFileStatus {
    STAGED,
    UNTRACKED,
}

enum class ReviewLineType {
    CONTEXT,
    ADDITION,
    DELETION,
    METADATA,
}

data class ReviewLine(
    val number: Int?,
    val type: ReviewLineType,
    val text: String,
)

data class ReviewFile(
    val path: String,
    val status: ReviewFileStatus,
    val lines: List<ReviewLine>,
)

data class ReviewComment(
    val id: String,
    val filePath: String,
    val line: Int?,
    val body: String,
    val required: Boolean,
)

data class ReviewState(
    val projectRoot: String,
    val files: List<ReviewFile> = emptyList(),
    val selectedFilePath: String? = null,
    val comments: List<ReviewComment> = emptyList(),
    val exportedCommentaries: String? = null,
    val activeMcpSessionId: String? = null,
    val errorMessage: String? = null,
) {
    val canComplete: Boolean
        get() = comments.isNotEmpty()
}

fun interface CodeNavigator {
    fun open(filePath: String, line: Int?)
}

interface ReviewComponent {
    val state: Value<ReviewState>

    fun reload()
    fun selectFile(path: String)
    fun addComment(filePath: String, line: Int?, body: String, required: Boolean)
    fun exportCommentaries(): String
    fun saveThroughMcp(): String?
    fun completeWebReview(sessionId: String): Boolean
    fun openFile(filePath: String, line: Int?)
}
