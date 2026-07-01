package com.aequicor.missionreview.core.git

/**
 * Git change summary displayed in the local review screen.
 *
 * @property path Repository-relative path.
 * @property statusCode Raw two-column porcelain status code.
 * @property statusLabel Human-readable status label.
 */
data class ProjectChange(
    val path: String,
    val statusCode: String,
    val statusLabel: String,
)
