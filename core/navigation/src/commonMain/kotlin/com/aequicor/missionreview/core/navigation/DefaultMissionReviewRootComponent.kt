package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value

/**
 * Default Decompose root component.
 *
 * [componentContext] is supplied by the target app because the target owns
 * lifecycle integration. [start] selects the first stack configuration without
 * leaking target UI classes into `core:navigation`.
 */
class DefaultMissionReviewRootComponent(
    componentContext: ComponentContext,
    start: MissionReviewStart,
    private val projectServices: MissionReviewProjectServices = MissionReviewProjectServices(),
) : MissionReviewRootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<MissionReviewConfig>()

    override val childStack: Value<ChildStack<*, MissionReviewChild>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = start.toInitialConfig(),
            handleBackButton = true,
            childFactory = ::createChild,
        )

    override fun onBackClicked() = navigation.pop()

    private fun createChild(
        config: MissionReviewConfig,
        componentContext: ComponentContext,
    ): MissionReviewChild =
        when (config) {
            ProjectSelectionConfig -> createProjectSelectionChild(componentContext)
            is LocalReviewConfig -> createLocalReviewChild(config, componentContext)
        }

    private fun createProjectSelectionChild(componentContext: ComponentContext): MissionReviewChild =
        ProjectSelectionChild(
            component =
                DefaultProjectSelectionComponent(
                    componentContext = componentContext,
                    directoryPicker = projectServices.directoryPicker,
                    recentProjectsStore = projectServices.recentProjectsStore,
                    onOpenReview = { projectPath ->
                        navigation.pushNew(
                            LocalReviewConfig(
                                projectPath = projectPath,
                                canNavigateBack = true,
                            ),
                        )
                    },
                ),
        )

    private fun createLocalReviewChild(
        config: LocalReviewConfig,
        componentContext: ComponentContext,
    ): MissionReviewChild =
        LocalReviewChild(
            component =
                DefaultLocalReviewComponent(
                    componentContext = componentContext,
                    projectPath = config.projectPath,
                    canNavigateBack = config.canNavigateBack,
                    fallbackDescription = config.description,
                    projectInspector = projectServices.projectInspector,
                    projectInspectionExecutor = projectServices.projectInspectionExecutor,
                    onBackRequested = navigation::pop,
                ),
        )
}
