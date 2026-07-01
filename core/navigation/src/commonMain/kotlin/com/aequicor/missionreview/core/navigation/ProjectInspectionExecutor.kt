package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.ProjectInspection

/**
 * Runs project inspection work away from target UI event handlers.
 */
interface ProjectInspectionExecutor {

    /**
     * Starts [operation] and delivers its result to [onResult].
     */
    fun execute(
        operation: () -> ProjectInspection,
        onResult: (Result<ProjectInspection>) -> Unit,
    ): ProjectInspectionTask
}

/**
 * Cancellable in-flight project inspection.
 */
fun interface ProjectInspectionTask {

    /**
     * Cancels the task if it has not completed yet.
     */
    fun cancel()
}

/**
 * Synchronous executor used by tests and targets that do not provide background IO.
 */
object ImmediateProjectInspectionExecutor : ProjectInspectionExecutor {

    override fun execute(
        operation: () -> ProjectInspection,
        onResult: (Result<ProjectInspection>) -> Unit,
    ): ProjectInspectionTask {
        onResult(runCatching(operation))

        return ProjectInspectionTask {}
    }
}
