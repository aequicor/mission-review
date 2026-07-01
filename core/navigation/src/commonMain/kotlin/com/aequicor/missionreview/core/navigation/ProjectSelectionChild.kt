package com.aequicor.missionreview.core.navigation

/**
 * Root child for the desktop project-selection placeholder.
 *
 * @property component Component rendered by target UI adapters.
 */
data class ProjectSelectionChild(
    val component: ProjectSelectionComponent,
) : MissionReviewChild
