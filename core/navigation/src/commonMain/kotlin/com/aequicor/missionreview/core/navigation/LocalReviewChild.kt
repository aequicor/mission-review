package com.aequicor.missionreview.core.navigation

/**
 * Root child for the local review placeholder.
 *
 * @property component Component rendered by target UI adapters.
 */
data class LocalReviewChild(
    val component: LocalReviewComponent,
) : MissionReviewChild
