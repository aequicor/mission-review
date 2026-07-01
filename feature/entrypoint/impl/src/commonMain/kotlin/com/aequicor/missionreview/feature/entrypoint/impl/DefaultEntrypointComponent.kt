package com.aequicor.missionreview.feature.entrypoint.impl

import com.aequicor.missionreview.feature.entrypoint.api.EntrypointComponent
import com.aequicor.missionreview.feature.entrypoint.api.EntrypointState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default entrypoint component that validates a project path and opens review.
 */
class DefaultEntrypointComponent(
    componentContext: ComponentContext,
    private val onOpenReview: (projectRoot: String) -> Unit,
) : EntrypointComponent,
    ComponentContext by componentContext {

    private val mutableState = MutableValue(EntrypointState())

    override val state: Value<EntrypointState> = mutableState

    override fun selectProject(path: String) {
        mutableState.value = mutableState.value.copy(
            projectPath = path,
            errorMessage = null,
        )
    }

    override fun openSelectedProject() {
        val projectPath = mutableState.value.projectPath.trim()
        if (projectPath.isBlank()) {
            mutableState.value = mutableState.value.copy(errorMessage = "Project path is required")
            return
        }

        onOpenReview(projectPath)
    }
}
