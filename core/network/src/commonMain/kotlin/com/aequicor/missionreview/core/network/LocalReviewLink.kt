package com.aequicor.missionreview.core.network

/**
 * Configuration for local review links.
 *
 * @property scheme URL scheme.
 * @property host local server host.
 * @property port local server port.
 */
data class LocalReviewServerConfig(
    val scheme: String = "http",
    val host: String = "127.0.0.1",
    val port: Int = 8765,
)

/**
 * Local review route for a session.
 *
 * @property sessionId local review session id.
 * @property path URL path for the review UI.
 */
data class LocalReviewRoute(
    val sessionId: String,
    val path: String,
)

/**
 * Builds local links for human review sessions.
 */
class LocalReviewLinkBuilder(
    private val config: LocalReviewServerConfig = LocalReviewServerConfig(),
) {
    /**
     * Builds a route for [sessionId].
     */
    fun routeFor(sessionId: String): LocalReviewRoute =
        LocalReviewRoute(
            sessionId = sessionId,
            path = "/review/$sessionId",
        )

    /**
     * Builds an absolute local URL for [sessionId].
     */
    fun linkFor(sessionId: String): String {
        val route = routeFor(sessionId)
        return "${config.scheme}://${config.host}:${config.port}${route.path}"
    }
}
