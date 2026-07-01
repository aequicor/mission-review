package com.aequicor.missionreview.core.navigation

/**
 * Stack configuration for the local-review placeholder child.
 *
 * @property canNavigateBack Whether the rendered review placeholder should expose back navigation.
 * @property description Placeholder text rendered by target UI adapters.
 */
internal data class LocalReviewConfig(
    val canNavigateBack: Boolean,
    val description: String = "Review UI is not implemented yet.",
) : MissionReviewConfig
