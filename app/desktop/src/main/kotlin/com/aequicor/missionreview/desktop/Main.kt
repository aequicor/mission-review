package com.aequicor.missionreview.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aequicor.missionreview.core.di.MissionReviewRootFactory
import com.aequicor.missionreview.core.git.JvmGitChangeReader
import com.aequicor.missionreview.core.mcp.LocalReviewMcpGateway
import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.InMemoryReviewSessionStore
import com.aequicor.missionreview.feature.review.api.CodeNavigator
import com.aequicor.missionreview.ui.compose.MissionReviewDesktopApp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start

fun main() = application {
    val lifecycle = LifecycleRegistry().also {
        it.create()
        it.start()
        it.resume()
    }
    val rootFactory = MissionReviewRootFactory(
        gitChangeReader = JvmGitChangeReader(),
        mcpGateway = LocalReviewMcpGateway(
            store = InMemoryReviewSessionStore(),
            linkBuilder = LocalReviewLinkBuilder(),
        ),
        codeNavigator = CodeNavigator { _, _ -> },
    )
    val rootComponent = rootFactory.create(DefaultComponentContext(lifecycle))

    Window(
        onCloseRequest = ::exitApplication,
        title = "mission-review",
    ) {
        MissionReviewDesktopApp(rootComponent)
    }
}
