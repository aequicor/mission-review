package com.aequicor.missionreview.core.git

/**
 * Reads a local project snapshot for the review screen.
 */
interface ProjectInspector {

    /**
     * Inspects [projectPath] and returns tree, Git changes and selected diff state.
     */
    fun inspectProject(
        projectPath: String,
        selectedChangePath: String? = null,
    ): ProjectInspection
}

/**
 * Fallback inspector for targets that have not wired project IO yet.
 */
object EmptyProjectInspector : ProjectInspector {

    override fun inspectProject(
        projectPath: String,
        selectedChangePath: String?,
    ): ProjectInspection =
        ProjectInspection(
            projectPath = projectPath,
            projectName = projectPath.ifBlank { "Project" },
            message = "Project inspection is not available in this target.",
        )
}
