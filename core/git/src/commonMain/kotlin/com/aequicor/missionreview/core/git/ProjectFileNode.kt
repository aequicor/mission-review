package com.aequicor.missionreview.core.git

/**
 * File tree node rendered by target UI adapters.
 *
 * @property name Display name.
 * @property path Project-relative path. The root-level entries use their own relative path.
 * @property isDirectory Whether this node represents a directory.
 * @property children Child nodes for directories.
 */
data class ProjectFileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<ProjectFileNode> = emptyList(),
)
