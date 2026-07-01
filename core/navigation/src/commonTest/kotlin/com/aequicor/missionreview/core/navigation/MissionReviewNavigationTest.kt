package com.aequicor.missionreview.core.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MissionReviewNavigationTest {

    @Test
    fun desktopStartOpensProjectSelection() {
        val root = createRoot(start = MissionReviewStart.DesktopCompose)

        val child = root.childStack.value.active.instance

        assertIs<ProjectSelectionChild>(child)
        assertEquals("Project selection", child.component.model.value.title)
    }

    @Test
    fun intellijStartWithProjectOpensLocalReview() {
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "C:/work/mission-review",
                ),
            )

        val child = root.childStack.value.active.instance

        assertIs<LocalReviewChild>(child)
        assertEquals("Local review", child.component.model.value.title)
        assertFalse(child.component.model.value.canNavigateBack)
    }

    @Test
    fun intellijStartWithoutProjectPathStillOpensLocalReviewForCurrentProject() {
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "",
                ),
            )

        val child = root.childStack.value.active.instance

        assertIs<LocalReviewChild>(child)
        assertFalse(child.component.model.value.canNavigateBack)
        assertEquals(
            "Open a project in IntelliJ IDEA before using Local review.",
            child.component.model.value.description,
        )
    }

    @Test
    fun projectSelectionOpensReviewAndReviewCanNavigateBack() {
        val root = createRoot(start = MissionReviewStart.DesktopCompose)
        val selection =
            assertIs<ProjectSelectionChild>(
                root.childStack.value.active.instance,
            ).component

        selection.onOpenProjectClicked()

        val review =
            assertIs<LocalReviewChild>(
                root.childStack.value.active.instance,
            ).component
        assertEquals("Local review", review.model.value.title)
        assertTrue(review.model.value.canNavigateBack)

        review.onBackClicked()

        assertIs<ProjectSelectionChild>(root.childStack.value.active.instance)
    }

    private fun createRoot(start: MissionReviewStart): MissionReviewRootComponent =
        DefaultMissionReviewRootComponent(
            componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
            start = start,
        )
}
