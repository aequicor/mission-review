package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.feature.entrypoint.api.EntrypointComponent
import com.aequicor.missionreview.feature.review.api.ReviewComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

sealed interface MissionReviewRoute {
    data object Entrypoint : MissionReviewRoute
    data class Review(val projectRoot: String) : MissionReviewRoute
}

sealed interface MissionReviewNavigationCommand {
    data class OpenReview(val projectRoot: String) : MissionReviewNavigationCommand
    data object BackToEntrypoint : MissionReviewNavigationCommand
}

sealed interface MissionReviewChild {
    data class Entrypoint(val component: EntrypointComponent) : MissionReviewChild
    data class Review(val component: ReviewComponent) : MissionReviewChild
}

interface MissionReviewRootComponent {
    val route: Value<MissionReviewRoute>
    val child: Value<MissionReviewChild>

    fun accept(command: MissionReviewNavigationCommand)
}

class DefaultMissionReviewRootComponent(
    componentContext: ComponentContext,
    private val entrypointFactory: (ComponentContext, (String) -> Unit) -> EntrypointComponent,
    private val reviewFactory: (ComponentContext, String) -> ReviewComponent,
) : MissionReviewRootComponent,
    ComponentContext by componentContext {

    private val mutableRoute = MutableValue<MissionReviewRoute>(MissionReviewRoute.Entrypoint)
    private val mutableChild = MutableValue<MissionReviewChild>(
        MissionReviewChild.Entrypoint(
            entrypointFactory(componentContext) { projectRoot ->
                accept(MissionReviewNavigationCommand.OpenReview(projectRoot))
            },
        ),
    )

    override val route: Value<MissionReviewRoute> = mutableRoute
    override val child: Value<MissionReviewChild> = mutableChild

    override fun accept(command: MissionReviewNavigationCommand) {
        when (command) {
            MissionReviewNavigationCommand.BackToEntrypoint -> showEntrypoint()
            is MissionReviewNavigationCommand.OpenReview -> showReview(command.projectRoot)
        }
    }

    private fun showEntrypoint() {
        mutableRoute.value = MissionReviewRoute.Entrypoint
        mutableChild.value = MissionReviewChild.Entrypoint(
            entrypointFactory(this) { projectRoot ->
                accept(MissionReviewNavigationCommand.OpenReview(projectRoot))
            },
        )
    }

    private fun showReview(projectRoot: String) {
        mutableRoute.value = MissionReviewRoute.Review(projectRoot)
        mutableChild.value = MissionReviewChild.Review(reviewFactory(this, projectRoot))
    }
}
