package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.storage.RecentProject
import com.aequicor.missionreview.core.storage.RecentProjectsStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default implementation of the desktop project-selection flow.
 *
 * [onOpenReview] is injected by the root component so this child does not own
 * stack navigation directly.
 */
internal class DefaultProjectSelectionComponent(
    componentContext: ComponentContext,
    private val directoryPicker: ProjectDirectoryPicker,
    private val recentProjectsStore: RecentProjectsStore,
    private val onOpenReview: (String) -> Unit,
) : ProjectSelectionComponent, ComponentContext by componentContext {

    private val mutableModel =
        MutableValue(
            ProjectSelectionModel(
                recentProjects = recentProjectsStore.loadRecentProjects(),
            ),
        )

    override val model: Value<ProjectSelectionModel> = mutableModel

    override fun onChooseProjectClicked() {
        val selectedPath =
            runCatching { directoryPicker.chooseProjectDirectory() }
                .getOrElse { throwable ->
                    showError(throwable.message ?: "Unable to open project picker.")
                    null
                }

        if (selectedPath.isNullOrBlank()) {
            return
        }

        openProject(selectedPath)
    }

    override fun onRecentProjectClicked(project: RecentProject) {
        openProject(project.path)
    }

    override fun onDismissErrorClicked() {
        mutableModel.value = mutableModel.value.copy(errorMessage = null)
    }

    private fun openProject(path: String) {
        val recentProjects =
            runCatching { recentProjectsStore.rememberProject(path) }
                .getOrElse { throwable ->
                    showError("Unable to remember project: ${throwable.message ?: "storage error"}.")
                    return
                }
        mutableModel.value =
            mutableModel.value.copy(
                recentProjects = recentProjects,
                errorMessage = null,
            )
        onOpenReview(path)
    }

    private fun showError(message: String) {
        mutableModel.value = mutableModel.value.copy(errorMessage = message)
    }
}
