package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.value.Value

/**
 * Placeholder contract for the local review flow.
 */
interface LocalReviewComponent {

    /**
     * Static placeholder model for the current skeleton screen.
     */
    val model: Value<LocalReviewModel>

    /**
     * Requests navigation back when the target flow supports it.
     */
    fun onBackClicked()
}
