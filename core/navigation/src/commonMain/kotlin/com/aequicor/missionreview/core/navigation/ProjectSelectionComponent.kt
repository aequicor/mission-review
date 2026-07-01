package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.storage.RecentProject
import com.arkivanov.decompose.value.Value

/**
 * Contract for selecting a project in the standalone desktop flow.
 */
interface ProjectSelectionComponent {

    /**
     * Current project-selection screen state.
     */
    val model: Value<ProjectSelectionModel>

    /**
     * Opens the target directory picker.
     */
    fun onChooseProjectClicked()

    /**
     * Opens a previously remembered project.
     */
    fun onRecentProjectClicked(project: RecentProject)

    /**
     * Clears a recoverable error message.
     */
    fun onDismissErrorClicked()
}
