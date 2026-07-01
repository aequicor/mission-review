package com.aequicor.missionreview.core.coderender

import com.aequicor.missionreview.core.git.GitChangedFile
import com.aequicor.missionreview.core.git.GitChangeStatus

enum class RenderedFileStatus {
    STAGED,
    UNTRACKED,
}

enum class RenderedLineType {
    CONTEXT,
    ADDITION,
    DELETION,
    METADATA,
}

data class RenderedLine(
    val number: Int?,
    val type: RenderedLineType,
    val text: String,
)

data class RenderedReviewFile(
    val path: String,
    val status: RenderedFileStatus,
    val lines: List<RenderedLine>,
)

fun GitChangedFile.toRenderedReviewFile(): RenderedReviewFile =
    RenderedReviewFile(
        path = path,
        status = status.toRenderedStatus(),
        lines = when (status) {
            GitChangeStatus.STAGED -> diff.orEmpty().toDiffLines()
            GitChangeStatus.UNTRACKED -> content.orEmpty().toContentLines()
        },
    )

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
