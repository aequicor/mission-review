package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.storage.RecentProject

/**
 * State for the desktop project-selection screen.
 *
 * @property title Screen title rendered by target UI adapters.
 * @property description Message rendered below the title.
 * @property recentProjects Previously opened local projects.
 * @property errorMessage Optional recoverable error message.
 */
data class ProjectSelectionModel(
    val title: String = "Open project",
    val description: String = "Choose a local Git project to review changes.",
    val recentProjects: List<RecentProject> = emptyList(),
    val errorMessage: String? = null,
)
