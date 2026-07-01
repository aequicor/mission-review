package com.aequicor.missionreview.core.git

data class GitRepository(
    val rootPath: String,
)

enum class GitChangeStatus {
    STAGED,
    UNTRACKED,
}

data class GitChangedFile(
    val path: String,
    val status: GitChangeStatus,
    val diff: String? = null,
    val content: String? = null,
)

interface GitChangeReader {
    fun readChanges(repository: GitRepository): List<GitChangedFile>
}
