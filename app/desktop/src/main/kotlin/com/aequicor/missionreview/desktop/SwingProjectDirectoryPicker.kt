package com.aequicor.missionreview.desktop

import com.aequicor.missionreview.core.navigation.ProjectDirectoryPicker
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Desktop directory picker backed by Swing's platform file chooser.
 */
class SwingProjectDirectoryPicker : ProjectDirectoryPicker {

    override fun chooseProjectDirectory(): String? =
        runOnEventDispatchThread {
            useSystemLookAndFeel()

            val chooser =
                JFileChooser().apply {
                    dialogTitle = "Choose project"
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    isAcceptAllFileFilterUsed = false
                    currentDirectory = File(System.getProperty("user.home"))
                }

            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.absolutePath
            } else {
                null
            }
        }

    private fun useSystemLookAndFeel() {
        runCatching {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }
    }

    private fun <T> runOnEventDispatchThread(block: () -> T): T {
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
}
