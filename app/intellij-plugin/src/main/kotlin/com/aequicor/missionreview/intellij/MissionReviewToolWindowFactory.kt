package com.aequicor.missionreview.intellij

class MissionReviewToolWindowFactory {
    fun create(projectRoot: String): IntellijLocalReviewHandle =
        MissionReviewIntellijPluginEntrypoint().createLocalReview(projectRoot)
}
