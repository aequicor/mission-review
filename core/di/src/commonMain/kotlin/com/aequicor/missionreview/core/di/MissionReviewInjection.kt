package com.aequicor.missionreview.core.di

import com.aequicor.missionreview.core.git.GitChangeReader
import com.aequicor.missionreview.core.mcp.LocalReviewMcpGateway
import com.aequicor.missionreview.core.mcp.ReviewMcpGateway
import com.aequicor.missionreview.core.navigation.DefaultMissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.network.LocalReviewLinkBuilder
import com.aequicor.missionreview.core.storage.InMemoryReviewSessionStore
import com.aequicor.missionreview.core.storage.ReviewSessionStore
import com.aequicor.missionreview.feature.entrypoint.impl.DefaultEntrypointComponent
import com.aequicor.missionreview.feature.review.api.CodeNavigator
import com.aequicor.missionreview.feature.review.impl.DefaultReviewComponent
import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Target-provided bindings required by the shared review graph.
 *
 * @property gitChangeReader target Git reader implementation.
 * @property codeNavigator target code navigation callback.
 */
data class MissionReviewTargetBindings(
    val gitChangeReader: GitChangeReader,
    val codeNavigator: CodeNavigator = CodeNavigator { _, _ -> },
)

/**
 * Creates the shared Koin module for mission-review.
 */
fun missionReviewModule(targetBindings: MissionReviewTargetBindings): Module =
    module {
        single { targetBindings }
        single<GitChangeReader> { targetBindings.gitChangeReader }
        single<CodeNavigator> { targetBindings.codeNavigator }
        single<ReviewSessionStore> { InMemoryReviewSessionStore() }
        single { LocalReviewLinkBuilder() }
        single<ReviewMcpGateway> { LocalReviewMcpGateway(get(), get()) }
        single {
            MissionReviewRootFactory(
                gitChangeReader = get(),
                mcpGateway = get(),
                codeNavigator = get(),
            )
        }
    }

/**
 * Factory that creates the root Decompose component with target dependencies.
 */
class MissionReviewRootFactory(
    private val gitChangeReader: GitChangeReader,
    private val mcpGateway: ReviewMcpGateway,
    private val codeNavigator: CodeNavigator,
) {
    /**
     * Creates a root component bound to [componentContext].
     */
    fun create(componentContext: ComponentContext): MissionReviewRootComponent =
        DefaultMissionReviewRootComponent(
            componentContext = componentContext,
            entrypointFactory = { childContext, onOpenReview ->
                DefaultEntrypointComponent(
                    componentContext = childContext,
                    onOpenReview = onOpenReview,
                )
            },
            reviewFactory = { childContext, projectRoot ->
                DefaultReviewComponent(
                    componentContext = childContext,
                    projectRoot = projectRoot,
                    gitChangeReader = gitChangeReader,
                    mcpGateway = mcpGateway,
                    navigator = codeNavigator,
                )
            },
        )
}
