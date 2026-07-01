package com.aequicor.missionreview.core.navigation

/**
 * Dumb placeholder state for the local review screen.
 *
 * @property title Screen title rendered by target UI adapters.
 * @property description Placeholder message rendered below the title.
 * @property canNavigateBack Whether the target UI should expose a back action.
 */
data class LocalReviewModel(
    val title: String = "Local review",
    val description: String = "Review UI is not implemented yet.",
    val canNavigateBack: Boolean = false,
)
