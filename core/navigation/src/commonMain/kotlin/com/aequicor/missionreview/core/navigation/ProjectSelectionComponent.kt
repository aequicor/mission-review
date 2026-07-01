package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.value.Value

/**
 * Placeholder contract for selecting a project in the standalone desktop flow.
 */
interface ProjectSelectionComponent {

    /**
     * Static placeholder model for the current skeleton screen.
     */
    val model: Value<ProjectSelectionModel>

    /**
     * Advances to the review placeholder.
     */
    fun onOpenProjectClicked()
}
