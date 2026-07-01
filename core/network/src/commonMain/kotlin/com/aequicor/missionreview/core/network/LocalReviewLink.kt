package com.aequicor.missionreview.core.network

data class LocalReviewServerConfig(
    val scheme: String = "http",
    val host: String = "127.0.0.1",
    val port: Int = 8765,
)

data class LocalReviewRoute(
    val sessionId: String,
    val path: String,
)

class LocalReviewLinkBuilder(
    private val config: LocalReviewServerConfig = LocalReviewServerConfig(),
) {
    fun routeFor(sessionId: String): LocalReviewRoute =
        LocalReviewRoute(
            sessionId = sessionId,
            path = "/review/$sessionId",
        )

    fun linkFor(sessionId: String): String {
        val route = routeFor(sessionId)
        return "${config.scheme}://${config.host}:${config.port}${route.path}"
    }
}
