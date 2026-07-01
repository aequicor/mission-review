package com.aequicor.missionreview.intellij

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop

/**
 * Bridges the IntelliJ plugin lifetime to a Decompose [LifecycleRegistry].
 */
class IntellijLifecycleBridge {
    private val lifecycle = LifecycleRegistry().also {
        it.create()
        it.start()
        it.resume()
    }

    /**
     * Component context used as the root context for the plugin review flow.
     */
    val componentContext = DefaultComponentContext(lifecycle)

    /**
     * Moves the Decompose lifecycle to destroyed state when the IDE-owned handle is disposed.
     */
    fun dispose() {
        lifecycle.pause()
        lifecycle.stop()
        lifecycle.destroy()
    }
}
