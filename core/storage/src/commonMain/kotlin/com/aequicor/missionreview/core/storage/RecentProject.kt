package com.aequicor.missionreview.core.storage

/**
 * Project directory remembered by the standalone desktop flow.
 *
 * @property path Absolute project directory path.
 * @property name Display name derived from the directory name.
 * @property lastOpenedAtEpochMillis Wall-clock timestamp of the latest opening.
 */
data class RecentProject(
    val path: String,
    val name: String,
    val lastOpenedAtEpochMillis: Long,
)
