package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default implementation of the local-review placeholder.
 *
 * [onBackRequested] is injected by the root component so this child can expose
 * a back intent without owning stack navigation directly.
 */
internal class DefaultLocalReviewComponent(
    componentContext: ComponentContext,
    model: LocalReviewModel,
    private val onBackRequested: () -> Unit,
) : LocalReviewComponent, ComponentContext by componentContext {

    override val model: Value<LocalReviewModel> =
        MutableValue(model)

    override fun onBackClicked() = onBackRequested()
}
