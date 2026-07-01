package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.ProjectInspection
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * JVM background executor for project inspection.
 *
 * [resultDispatcher] must deliver callbacks to the target UI thread.
 */
class JvmProjectInspectionExecutor(
    private val resultDispatcher: (() -> Unit) -> Unit,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) : ProjectInspectionExecutor, AutoCloseable {

    private val workerExecutor =
        Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "mission-review-project-inspection").apply {
                isDaemon = true
            }
        }
    private val timeoutExecutor =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "mission-review-project-inspection-timeout").apply {
                isDaemon = true
            }
        }

    override fun execute(
        operation: () -> ProjectInspection,
        onResult: (Result<ProjectInspection>) -> Unit,
    ): ProjectInspectionTask {
        val delivered = AtomicBoolean(false)
        var workerFuture: Future<*>? = null
        var timeoutFuture: ScheduledFuture<*>? = null

        fun deliver(result: Result<ProjectInspection>) {
            if (delivered.compareAndSet(false, true)) {
                timeoutFuture?.cancel(false)
                resultDispatcher {
                    onResult(result)
                }
            }
        }

        workerFuture =
            workerExecutor.submit {
                deliver(runCatching(operation))
            }
        timeoutFuture =
            timeoutExecutor.schedule(
                {
                    if (delivered.compareAndSet(false, true)) {
                        workerFuture?.cancel(true)
                        resultDispatcher {
                            onResult(
                                Result.failure(
                                    ProjectInspectionTimeoutException(timeoutMillis = timeoutMillis),
                                ),
                            )
                        }
                    }
                },
                timeoutMillis,
                TimeUnit.MILLISECONDS,
            )

        return ProjectInspectionTask {
            if (delivered.compareAndSet(false, true)) {
                workerFuture?.cancel(true)
                timeoutFuture?.cancel(false)
            }
        }
    }

    override fun close() {
        workerExecutor.shutdownNow()
        timeoutExecutor.shutdownNow()
    }

    private companion object {
        private const val DEFAULT_TIMEOUT_MILLIS = 15_000L
    }
}

/**
 * Failure returned when project inspection does not finish in time.
 */
class ProjectInspectionTimeoutException(
    timeoutMillis: Long,
) : RuntimeException("Project inspection timed out after ${timeoutMillis}ms.")
