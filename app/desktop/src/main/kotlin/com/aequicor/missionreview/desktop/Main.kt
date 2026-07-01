package com.aequicor.missionreview.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.aequicor.missionreview.core.navigation.DefaultMissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.MissionReviewStart
import com.aequicor.missionreview.ui.compose.MissionReviewRootContent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import javax.swing.SwingUtilities

fun main() {
    val lifecycle = LifecycleRegistry()
    val root = createRootOnUiThread(lifecycle)

    application {
        val windowState = rememberWindowState(width = 1100.dp, height = 720.dp)

        LifecycleController(lifecycleRegistry = lifecycle, windowState = windowState)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "mission-review",
        ) {
            MaterialTheme {
                Surface {
                    MissionReviewRootContent(
                        component = root,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

private fun createRootOnUiThread(lifecycle: LifecycleRegistry): MissionReviewRootComponent =
    runOnUiThread {
        DefaultMissionReviewRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            start = MissionReviewStart.DesktopCompose,
        )
    }

private fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var result: T? = null
    var failure: Throwable? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (throwable: Throwable) {
            failure = throwable
        }
    }

    failure?.let { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
