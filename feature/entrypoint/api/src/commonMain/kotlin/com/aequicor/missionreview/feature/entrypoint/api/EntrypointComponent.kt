package com.aequicor.missionreview.feature.entrypoint.api

import com.arkivanov.decompose.value.Value

/**
 * State of the project selection entrypoint.
 *
 * @property projectPath path entered or selected by the user.
 * @property errorMessage last validation error shown to the user.
 */
data class EntrypointState(
    val projectPath: String = "",
    val errorMessage: String? = null,
) {
    /**
     * Whether the current state can open a review flow.
     */
    val canOpenReview: Boolean
        get() = projectPath.isNotBlank()
}

/**
 * Public component contract for the project selection flow.
 */
interface EntrypointComponent {
    /**
     * Observable entrypoint state.
     */
    val state: Value<EntrypointState>

    /**
     * Updates the selected project path.
     */
    fun selectProject(path: String)

    /**
     * Opens the selected project when [EntrypointState.canOpenReview] is true.
     */
    fun openSelectedProject()
}
