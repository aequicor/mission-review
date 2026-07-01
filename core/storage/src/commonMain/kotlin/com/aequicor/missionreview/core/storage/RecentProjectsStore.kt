package com.aequicor.missionreview.core.storage

/**
 * Stores recently opened project directories.
 *
 * Implementations should keep the newest project first and de-duplicate by
 * absolute path.
 */
interface RecentProjectsStore {

    /**
     * Loads the currently remembered project directories.
     */
    fun loadRecentProjects(): List<RecentProject>

    /**
     * Persists [path] as the most recently opened project and returns the new list.
     */
    fun rememberProject(path: String): List<RecentProject>
}

/**
 * No-op store used by targets that do not own desktop recent projects.
 */
object EmptyRecentProjectsStore : RecentProjectsStore {

    override fun loadRecentProjects(): List<RecentProject> = emptyList()

    override fun rememberProject(path: String): List<RecentProject> = emptyList()
}
