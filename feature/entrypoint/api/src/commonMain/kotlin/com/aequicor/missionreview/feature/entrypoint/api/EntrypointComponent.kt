package com.aequicor.missionreview.feature.entrypoint.api

import com.arkivanov.decompose.value.Value

data class EntrypointState(
    val projectPath: String = "",
    val errorMessage: String? = null,
) {
    val canOpenReview: Boolean
        get() = projectPath.isNotBlank()
}

interface EntrypointComponent {
    val state: Value<EntrypointState>

    fun selectProject(path: String)
    fun openSelectedProject()
}
