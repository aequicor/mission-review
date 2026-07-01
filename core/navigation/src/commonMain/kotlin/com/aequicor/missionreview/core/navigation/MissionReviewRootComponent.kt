package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.feature.entrypoint.api.EntrypointComponent
import com.aequicor.missionreview.feature.review.api.ReviewComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Active top-level route of the mission-review flow.
 */
sealed interface MissionReviewRoute {
    /**
     * Project selection route.
     */
    data object Entrypoint : MissionReviewRoute

    /**
     * Review route bound to [projectRoot].
     */
    data class Review(val projectRoot: String) : MissionReviewRoute
}

/**
 * Commands accepted by the root component navigation boundary.
 */
sealed interface MissionReviewNavigationCommand {
    /**
     * Opens review for [projectRoot].
     */
    data class OpenReview(val projectRoot: String) : MissionReviewNavigationCommand

    /**
     * Returns to project selection.
     */
    data object BackToEntrypoint : MissionReviewNavigationCommand
}

/**
 * Active child component rendered by UI adapters.
 */
sealed interface MissionReviewChild {
    /**
     * Entrypoint child.
     */
    data class Entrypoint(val component: EntrypointComponent) : MissionReviewChild

    /**
     * Review child.
     */
    data class Review(val component: ReviewComponent) : MissionReviewChild
}

/**
 * Root component contract shared by desktop and IntelliJ targets.
 */
interface MissionReviewRootComponent {
    /**
     * Observable active route.
     */
    val route: Value<MissionReviewRoute>

    /**
     * Observable active child component.
     */
    val child: Value<MissionReviewChild>

    /**
     * Applies a navigation [command].
     */
    fun accept(command: MissionReviewNavigationCommand)
}

/**
 * Default root component that owns top-level navigation state.
 */
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
