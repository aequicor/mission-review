package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.ProjectChange
import com.aequicor.missionreview.core.git.ProjectFileNode

/**
 * State for the local review screen.
 *
 * @property title Screen title rendered by target UI adapters.
 * @property description Status message rendered below the title.
 * @property canNavigateBack Whether the target UI should expose a back action.
 * @property projectPath Absolute local project path.
 * @property fileTree File tree shown in the left project panel.
 * @property changedFiles Git changed files shown in the left project panel.
 * @property selectedChangePath Currently selected changed file.
 * @property diffText Text diff or fallback preview shown in the main panel.
 * @property isLoading Whether project inspection is currently running.
 */
data class LocalReviewModel(
    val title: String = "Local review",
    val description: String = "Choose a changed file to inspect its diff.",
    val canNavigateBack: Boolean = false,
    val projectPath: String = "",
    val fileTree: List<ProjectFileNode> = emptyList(),
    val changedFiles: List<ProjectChange> = emptyList(),
    val selectedChangePath: String? = null,
    val diffText: String = "",
    val isLoading: Boolean = false,
)
