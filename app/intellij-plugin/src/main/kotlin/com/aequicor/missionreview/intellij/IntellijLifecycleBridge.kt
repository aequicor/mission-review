package com.aequicor.missionreview.intellij

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop

class IntellijLifecycleBridge {
    private val lifecycle = LifecycleRegistry().also {
        it.create()
        it.start()
        it.resume()
    }

    val componentContext = DefaultComponentContext(lifecycle)

    fun dispose() {
        lifecycle.pause()
        lifecycle.stop()
        lifecycle.destroy()
    }
}
