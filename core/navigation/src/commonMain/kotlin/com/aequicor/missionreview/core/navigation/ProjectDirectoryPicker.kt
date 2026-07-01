package com.aequicor.missionreview.core.navigation

/**
 * Target-specific directory picker used by the desktop project-selection flow.
 */
fun interface ProjectDirectoryPicker {

    /**
     * Opens a directory picker and returns the selected absolute path.
     */
    fun chooseProjectDirectory(): String?
}

/**
 * Picker used when a target does not support arbitrary project selection.
 */
object EmptyProjectDirectoryPicker : ProjectDirectoryPicker {

    override fun chooseProjectDirectory(): String? = null
}
