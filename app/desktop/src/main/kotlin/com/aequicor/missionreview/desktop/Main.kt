package com.aequicor.missionreview.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.aequicor.missionreview.core.di.MissionReviewRootFactory
import com.aequicor.missionreview.core.git.JvmGitChangeReader
import com.aequicor.missionreview.core.mcp.LocalReviewMcpGateway
import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.InMemoryReviewSessionStore
import com.aequicor.missionreview.feature.review.api.CodeNavigator
import com.aequicor.missionreview.ui.compose.MissionReviewDesktopApp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import javax.swing.SwingUtilities

fun main() {
    val lifecycle = LifecycleRegistry()
    val rootFactory = MissionReviewRootFactory(
        gitChangeReader = JvmGitChangeReader(),
        mcpGateway = LocalReviewMcpGateway(
            store = InMemoryReviewSessionStore(),
            linkBuilder = LocalReviewLinkBuilder(),
        ),
        codeNavigator = CodeNavigator { _, _ -> },
    )
    val rootComponent = runOnUiThread {
        rootFactory.create(DefaultComponentContext(lifecycle))
    }

    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "mission-review",
        ) {
            MissionReviewDesktopApp(rootComponent)
        }
    }
}

private fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var result: Result<T>? = null
    SwingUtilities.invokeAndWait {
        result = runCatching(block)
    }

    return requireNotNull(result).getOrThrow()
}
