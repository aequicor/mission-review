package com.aequicor.missionreview.intellij

/**
 * Entry point used by the IntelliJ tool window integration to create review tabs.
 */
class MissionReviewToolWindowFactory {
    /**
     * Creates a local review handle for [projectRoot].
     */
    fun create(projectRoot: String): IntellijLocalReviewHandle =
        MissionReviewIntellijPluginEntrypoint().createLocalReview(projectRoot)
}
