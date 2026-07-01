package com.aequicor.missionreview.core.git

/**
 * Local Git repository selected for review.
 *
 * @property rootPath absolute path to the repository root.
 */
data class GitRepository(
    val rootPath: String,
)

/**
 * Git status categories included in the first review scope.
 */
enum class GitChangeStatus {
    STAGED,
    UNTRACKED,
}

/**
 * File changed in a local Git repository.
 *
 * @property path repository-relative file path.
 * @property status status category included in review.
 * @property diff unified diff for staged files.
 * @property content full file content for untracked files.
 */
data class GitChangedFile(
    val path: String,
    val status: GitChangeStatus,
    val diff: String? = null,
    val content: String? = null,
)

/**
 * Reads files that should be included in local review.
 */
interface GitChangeReader {
    /**
     * Reads staged and untracked changes from [repository].
     */
    fun readChanges(repository: GitRepository): List<GitChangedFile>
}
