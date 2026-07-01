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

class MissionReviewIntellijPluginEntrypoint(
    private val codeNavigator: IntellijCodeNavigator = IntellijCodeNavigator { _, _ -> },
) {
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

data class IntellijLocalReviewHandle(
    val rootComponent: MissionReviewRootComponent,
    val adapter: LocalReviewTabAdapter,
    val lifecycleBridge: IntellijLifecycleBridge,
) {
    fun dispose() {
        lifecycleBridge.dispose()
    }
}
