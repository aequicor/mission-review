package com.aequicor.missionreview.core.navigation

/**
 * Target-specific start mode for the shared navigation root.
 */
sealed interface MissionReviewStart {

    /**
     * Standalone desktop starts from the project-selection placeholder.
     */
    data object DesktopCompose : MissionReviewStart

    /**
     * IntelliJ starts from the local review placeholder for the current IDE project.
     *
     * @property projectPath Base path of the current IntelliJ project. An empty
     * string means the IDE project does not expose a base path, so the UI must
     * ask the user to open a project before local review.
     */
    data class IntelliJPlatform(
        val projectPath: String,
    ) : MissionReviewStart
}
