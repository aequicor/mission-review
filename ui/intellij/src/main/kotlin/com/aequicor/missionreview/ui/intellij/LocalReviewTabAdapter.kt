package com.aequicor.missionreview.ui.intellij

import com.aequicor.missionreview.core.navigation.MissionReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewNavigationCommand
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent

/**
 * IntelliJ editor navigation callback.
 */
fun interface IntellijCodeNavigator {
    /**
     * Navigates to [filePath] at [line] when available.
     */
    fun navigate(filePath: String, line: Int?)
}

/**
 * Adapter used by the IntelliJ plugin target to interact with the review flow.
 */
class LocalReviewTabAdapter(
    private val rootComponent: MissionReviewRootComponent,
    private val codeNavigator: IntellijCodeNavigator,
) {
    /**
     * Opens review for [projectRoot].
     */
    fun openProject(projectRoot: String) {
        rootComponent.accept(MissionReviewNavigationCommand.OpenReview(projectRoot))
    }

    /**
     * Returns the tab title.
     */
    fun title(): String =
        "Local review"

    /**
     * Returns the active project root, if review is open.
     */
    fun selectedProjectRoot(): String? =
        (rootComponent.child.value as? MissionReviewChild.Review)
            ?.component
            ?.state
            ?.value
            ?.projectRoot

    /**
     * Navigates to the currently selected review file.
     */
    fun navigateToSelectedFile() {
        val review = rootComponent.child.value as? MissionReviewChild.Review ?: return
        val selectedFilePath = review.component.state.value.selectedFilePath ?: return
        codeNavigator.navigate(selectedFilePath, null)
    }
}
