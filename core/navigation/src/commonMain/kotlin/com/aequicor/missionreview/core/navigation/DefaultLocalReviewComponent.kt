package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.ProjectInspection
import com.aequicor.missionreview.core.git.ProjectInspector
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy

/**
 * Default implementation of the local-review flow.
 *
 * [onBackRequested] is injected by the root component so this child can expose
 * a back intent without owning stack navigation directly.
 */
internal class DefaultLocalReviewComponent(
    componentContext: ComponentContext,
    private val projectPath: String,
    private val canNavigateBack: Boolean,
    private val fallbackDescription: String,
    private val projectInspector: ProjectInspector,
    private val projectInspectionExecutor: ProjectInspectionExecutor,
    private val onBackRequested: () -> Unit,
) : LocalReviewComponent, ComponentContext by componentContext {

    private val mutableModel = MutableValue(initialModel())
    private var currentInspectionTask: ProjectInspectionTask? = null
    private var loadRequestId = 0

    override val model: Value<LocalReviewModel> = mutableModel

    init {
        lifecycle.doOnDestroy {
            loadRequestId += 1
            currentInspectionTask?.cancel()
            currentInspectionTask = null
        }

        loadModel(selectedChangePath = null)
    }

    override fun onChangedFileClicked(path: String) {
        loadModel(selectedChangePath = path)
    }

    override fun onRefreshClicked() {
        loadModel(selectedChangePath = mutableModel.value.selectedChangePath)
    }

    override fun onBackClicked() {
        currentInspectionTask?.cancel()
        currentInspectionTask = null
        onBackRequested()
    }

    private fun loadModel(selectedChangePath: String?) {
        if (projectPath.isBlank()) {
            mutableModel.value = initialModel()
            return
        }

        currentInspectionTask?.cancel()
        loadRequestId += 1
        val currentRequestId = loadRequestId

        mutableModel.value =
            mutableModel.value.copy(
                description = "Loading project changes.",
                selectedChangePath = selectedChangePath,
                isLoading = true,
            )

        currentInspectionTask =
            projectInspectionExecutor.execute(
                operation = {
                    projectInspector.inspectProject(
                        projectPath = projectPath,
                        selectedChangePath = selectedChangePath,
                    )
                },
                onResult = { result ->
                    if (currentRequestId != loadRequestId) {
                        return@execute
                    }

                    currentInspectionTask = null
                    mutableModel.value =
                        result
                            .map { inspection -> inspection.toModel() }
                            .getOrElse(::failureModel)
                },
            )
    }

    private fun initialModel(): LocalReviewModel =
        LocalReviewModel(
            title = "Local review",
            description =
                if (projectPath.isBlank()) {
                    fallbackDescription
                } else {
                    "Loading project changes."
                },
            canNavigateBack = canNavigateBack,
            projectPath = projectPath,
            isLoading = projectPath.isNotBlank(),
        )

    private fun failureModel(throwable: Throwable): LocalReviewModel =
        LocalReviewModel(
            title = "Local review",
            description = throwable.message ?: "Unable to inspect selected project.",
            canNavigateBack = canNavigateBack,
            projectPath = projectPath,
        )

    private fun ProjectInspection.toModel(): LocalReviewModel =
        LocalReviewModel(
            title = projectName,
            description = message ?: "Reviewing local changes in $projectPath.",
            canNavigateBack = canNavigateBack,
            projectPath = projectPath,
            fileTree = fileTree,
            changedFiles = changedFiles,
            selectedChangePath = selectedChangePath,
            diffText = diffText,
        )
}
