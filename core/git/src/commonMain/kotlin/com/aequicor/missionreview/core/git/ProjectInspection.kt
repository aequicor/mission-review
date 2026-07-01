package com.aequicor.missionreview.core.git

/**
 * Snapshot of a local project used by the review screen.
 *
 * @property projectPath Absolute local project path.
 * @property projectName Display name derived from the project directory.
 * @property fileTree Project file tree.
 * @property changedFiles Git changed files.
 * @property selectedChangePath Path currently rendered in [diffText].
 * @property diffText Text diff or fallback preview for [selectedChangePath].
 * @property message Optional status message for empty, invalid or non-Git projects.
 */
data class ProjectInspection(
    val projectPath: String,
    val projectName: String,
    val fileTree: List<ProjectFileNode> = emptyList(),
    val changedFiles: List<ProjectChange> = emptyList(),
    val selectedChangePath: String? = null,
    val diffText: String = "",
    val message: String? = null,
)
