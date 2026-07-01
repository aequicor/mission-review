package com.aequicor.missionreview.core.navigation

/**
 * Stack configuration for the local-review placeholder child.
 *
 * @property projectPath Absolute project path inspected by this screen.
 * @property canNavigateBack Whether the rendered review placeholder should expose back navigation.
 * @property description Fallback text rendered when the project cannot be inspected.
 */
internal data class LocalReviewConfig(
    val projectPath: String,
    val canNavigateBack: Boolean,
    val description: String = "Choose a changed file to inspect its diff.",
) : MissionReviewConfig
