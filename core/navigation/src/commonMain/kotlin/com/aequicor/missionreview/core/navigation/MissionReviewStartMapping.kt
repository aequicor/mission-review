package com.aequicor.missionreview.core.navigation

/**
 * Maps a target start mode to the initial root stack configuration.
 */
internal fun MissionReviewStart.toInitialConfig(): MissionReviewConfig =
    when (this) {
        MissionReviewStart.DesktopCompose -> ProjectSelectionConfig
        is MissionReviewStart.IntelliJPlatform ->
            LocalReviewConfig(
                canNavigateBack = false,
                description = intellijDescriptionFor(projectPath = projectPath),
            )
    }

/**
 * Returns the placeholder message for the IntelliJ local-review start state.
 */
private fun intellijDescriptionFor(projectPath: String): String =
    if (projectPath.isBlank()) {
        "Open a project in IntelliJ IDEA before using Local review."
    } else {
        "Local review is bound to the current IntelliJ IDEA project."
    }
