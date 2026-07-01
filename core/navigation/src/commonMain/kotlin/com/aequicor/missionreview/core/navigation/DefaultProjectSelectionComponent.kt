package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * Default implementation of the desktop project-selection placeholder.
 *
 * [onOpenReview] is injected by the root component so this child does not own
 * stack navigation directly.
 */
internal class DefaultProjectSelectionComponent(
    componentContext: ComponentContext,
    private val onOpenReview: () -> Unit,
) : ProjectSelectionComponent, ComponentContext by componentContext {

    override val model: Value<ProjectSelectionModel> =
        MutableValue(ProjectSelectionModel())

    override fun onOpenProjectClicked() = onOpenReview()
}
