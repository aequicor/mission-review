package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/**
 * Root Decompose contract for the mission-review navigation tree.
 *
 * The root owns only navigation state. Target modules own lifecycle creation,
 * UI rendering, dependency wiring, and platform-specific actions.
 */
interface MissionReviewRootComponent {

    /**
     * Current stack of navigation children.
     */
    val childStack: Value<ChildStack<*, MissionReviewChild>>

    /**
     * Requests a stack pop from the active screen.
     */
    fun onBackClicked()
}
