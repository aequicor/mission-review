package com.aequicor.missionreview.core.coderender

import com.aequicor.missionreview.core.git.GitChangedFile
import com.aequicor.missionreview.core.git.GitChangeStatus

/**
 * File status after mapping Git data to review UI data.
 */
enum class RenderedFileStatus {
    STAGED,
    UNTRACKED,
}

/**
 * Rendered line category used by UI adapters.
 */
enum class RenderedLineType {
    CONTEXT,
    ADDITION,
    DELETION,
    METADATA,
}

/**
 * Rendered line shown in code or diff viewers.
 *
 * @property number one-based file line number when known.
 * @property type semantic line type.
 * @property text line content.
 */
data class RenderedLine(
    val number: Int?,
    val type: RenderedLineType,
    val text: String,
)

/**
 * Rendered file shown in a review UI.
 *
 * @property path repository-relative file path.
 * @property status rendered review status.
 * @property lines rendered file or diff lines.
 */
data class RenderedReviewFile(
    val path: String,
    val status: RenderedFileStatus,
    val lines: List<RenderedLine>,
)

/**
 * Maps a raw Git change to rendered review data.
 */
fun GitChangedFile.toRenderedReviewFile(): RenderedReviewFile =
    RenderedReviewFile(
        path = path,
        status = status.toRenderedStatus(),
        lines = when (status) {
            GitChangeStatus.STAGED -> diff.orEmpty().toDiffLines()
            GitChangeStatus.UNTRACKED -> content.orEmpty().toContentLines()
        },
    )

/**
 * Maps raw Git changes to rendered review data.
 */
fun List<GitChangedFile>.toRenderedReviewFiles(): List<RenderedReviewFile> =
    map(GitChangedFile::toRenderedReviewFile)

private fun GitChangeStatus.toRenderedStatus(): RenderedFileStatus =
    when (this) {
        GitChangeStatus.STAGED -> RenderedFileStatus.STAGED
        GitChangeStatus.UNTRACKED -> RenderedFileStatus.UNTRACKED
    }

private fun String.toContentLines(): List<RenderedLine> =
    lineSequence()
        .mapIndexed { index, text ->
            RenderedLine(
                number = index + 1,
                type = RenderedLineType.ADDITION,
                text = text,
            )
        }
        .toList()

private fun String.toDiffLines(): List<RenderedLine> =
    lineSequence()
        .map { text ->
            RenderedLine(
                number = null,
                type = when {
                    text.startsWith("@@") || text.startsWith("diff --git") || text.startsWith("index ") -> RenderedLineType.METADATA
                    text.startsWith("+") && !text.startsWith("+++") -> RenderedLineType.ADDITION
                    text.startsWith("-") && !text.startsWith("---") -> RenderedLineType.DELETION
                    else -> RenderedLineType.CONTEXT
                },
                text = text,
            )
        }
        .toList()
