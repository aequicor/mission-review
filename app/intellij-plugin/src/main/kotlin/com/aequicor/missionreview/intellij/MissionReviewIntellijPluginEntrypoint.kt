package com.aequicor.missionreview.intellij

import com.aequicor.missionreview.core.di.MissionReviewRootFactory
import com.aequicor.missionreview.core.git.JvmGitChangeReader
import com.aequicor.missionreview.core.mcp.LocalReviewMcpGateway
import com.aequicor.missionreview.core.navigation.MissionReviewNavigationCommand
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.InMemoryReviewSessionStore
import com.aequicor.missionreview.feature.review.api.CodeNavigator
import com.aequicor.missionreview.ui.intellij.IntellijCodeNavigator
import com.aequicor.missionreview.ui.intellij.LocalReviewTabAdapter

/**
 * Creates local-review sessions for the IntelliJ plugin target.
 *
 * @property codeNavigator target-specific editor navigation adapter.
 */
class MissionReviewIntellijPluginEntrypoint(
    private val codeNavigator: IntellijCodeNavigator = IntellijCodeNavigator { _, _ -> },
) {
    /**
     * Creates a review flow bound to [projectRoot].
     */
    fun createLocalReview(projectRoot: String): IntellijLocalReviewHandle {
        val lifecycleBridge = IntellijLifecycleBridge()
        val rootComponent = createRoot(lifecycleBridge)
        rootComponent.accept(MissionReviewNavigationCommand.OpenReview(projectRoot))

        return IntellijLocalReviewHandle(
            rootComponent = rootComponent,
            adapter = LocalReviewTabAdapter(rootComponent, codeNavigator),
            lifecycleBridge = lifecycleBridge,
        )
    }

    private fun createRoot(lifecycleBridge: IntellijLifecycleBridge): MissionReviewRootComponent {
        val rootFactory = MissionReviewRootFactory(
            gitChangeReader = JvmGitChangeReader(),
            mcpGateway = LocalReviewMcpGateway(
                store = InMemoryReviewSessionStore(),
                linkBuilder = LocalReviewLinkBuilder(),
            ),
            codeNavigator = CodeNavigator { filePath, line ->
                codeNavigator.navigate(filePath, line)
            },
        )

        return rootFactory.create(lifecycleBridge.componentContext)
    }
}

/**
 * Owns the plugin review flow objects that must be disposed together.
 *
 * @property rootComponent root Decompose component for the review flow.
 * @property adapter IntelliJ UI adapter for the review tab.
 * @property lifecycleBridge lifecycle bridge disposed with this handle.
 */
data class IntellijLocalReviewHandle(
    val rootComponent: MissionReviewRootComponent,
    val adapter: LocalReviewTabAdapter,
    val lifecycleBridge: IntellijLifecycleBridge,
) {
    /**
     * Releases lifecycle-bound Decompose resources.
     */
    fun dispose() {
        lifecycleBridge.dispose()
    }
}
