package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.EmptyProjectInspector
import com.aequicor.missionreview.core.git.ProjectInspector
import com.aequicor.missionreview.core.storage.EmptyRecentProjectsStore
import com.aequicor.missionreview.core.storage.RecentProjectsStore

/**
 * Project-related services injected by target apps into the shared root.
 */
data class MissionReviewProjectServices(
    val directoryPicker: ProjectDirectoryPicker = EmptyProjectDirectoryPicker,
    val recentProjectsStore: RecentProjectsStore = EmptyRecentProjectsStore,
    val projectInspector: ProjectInspector = EmptyProjectInspector,
    val projectInspectionExecutor: ProjectInspectionExecutor = ImmediateProjectInspectionExecutor,
)
