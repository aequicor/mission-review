package com.aequicor.missionreview.ui.intellij

import com.aequicor.missionreview.core.navigation.MissionReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewNavigationCommand
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent

fun interface IntellijCodeNavigator {
    fun navigate(filePath: String, line: Int?)
}

class LocalReviewTabAdapter(
    private val rootComponent: MissionReviewRootComponent,
    private val codeNavigator: IntellijCodeNavigator,
) {
    fun openProject(projectRoot: String) {
        rootComponent.accept(MissionReviewNavigationCommand.OpenReview(projectRoot))
    }

    fun title(): String =
        "Local review"

    fun selectedProjectRoot(): String? =
        (rootComponent.child.value as? MissionReviewChild.Review)
            ?.component
            ?.state
            ?.value
            ?.projectRoot

    fun navigateToSelectedFile() {
        val review = rootComponent.child.value as? MissionReviewChild.Review ?: return
        val selectedFilePath = review.component.state.value.selectedFilePath ?: return
        codeNavigator.navigate(selectedFilePath, null)
    }
}
