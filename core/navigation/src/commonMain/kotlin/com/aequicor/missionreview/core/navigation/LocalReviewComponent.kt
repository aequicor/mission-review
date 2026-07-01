package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.value.Value

/**
 * Contract for the local review flow.
 */
interface LocalReviewComponent {

    /**
     * Current review screen state.
     */
    val model: Value<LocalReviewModel>

    /**
     * Selects a changed file and refreshes the rendered diff.
     */
    fun onChangedFileClicked(path: String)

    /**
     * Re-reads project tree and Git changes.
     */
    fun onRefreshClicked()

    /**
     * Requests navigation back when the target flow supports it.
     */
    fun onBackClicked()
}
