package com.aequicor.missionreview.core.navigation

import com.aequicor.missionreview.core.git.ProjectChange
import com.aequicor.missionreview.core.git.ProjectFileNode
import com.aequicor.missionreview.core.git.ProjectInspection
import com.aequicor.missionreview.core.git.ProjectInspector
import com.aequicor.missionreview.core.storage.RecentProject
import com.aequicor.missionreview.core.storage.RecentProjectsStore
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MissionReviewNavigationTest {

    @Test
    fun desktopStartOpensProjectSelectionWithRecentProjects() {
        val recentProject =
            RecentProject(
                path = "C:/work/mission-review",
                name = "mission-review",
                lastOpenedAtEpochMillis = 1L,
            )
        val root =
            createRoot(
                start = MissionReviewStart.DesktopCompose,
                projectServices =
                    MissionReviewProjectServices(
                        recentProjectsStore = FakeRecentProjectsStore(listOf(recentProject)),
                    ),
            )

        val child = root.childStack.value.active.instance

        assertIs<ProjectSelectionChild>(child)
        assertEquals("Open project", child.component.model.value.title)
        assertEquals(listOf(recentProject), child.component.model.value.recentProjects)
    }

    @Test
    fun intellijStartWithProjectOpensLocalReview() {
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "C:/work/mission-review",
                ),
                projectServices = fakeProjectServices(),
            )

        val child = root.childStack.value.active.instance

        assertIs<LocalReviewChild>(child)
        assertEquals("mission-review", child.component.model.value.title)
        assertFalse(child.component.model.value.canNavigateBack)
        assertEquals("src/Main.kt", child.component.model.value.selectedChangePath)
    }

    @Test
    fun intellijStartWithoutProjectPathStillOpensLocalReviewForCurrentProject() {
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "",
                ),
                projectServices = fakeProjectServices(),
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
    fun projectSelectionChoosesProjectAndReviewCanNavigateBack() {
        val root =
            createRoot(
                start = MissionReviewStart.DesktopCompose,
                projectServices =
                    fakeProjectServices(
                        selectedPath = "C:/work/mission-review",
                    ),
            )
        val selection =
            assertIs<ProjectSelectionChild>(
                root.childStack.value.active.instance,
            ).component

        selection.onChooseProjectClicked()

        val review =
            assertIs<LocalReviewChild>(
                root.childStack.value.active.instance,
            ).component
        assertEquals("mission-review", review.model.value.title)
        assertTrue(review.model.value.canNavigateBack)
        assertEquals("src/Main.kt", review.model.value.selectedChangePath)

        review.onBackClicked()

        assertIs<ProjectSelectionChild>(root.childStack.value.active.instance)
    }

    @Test
    fun projectSelectionShowsErrorWhenRecentProjectCannotBeRemembered() {
        val root =
            createRoot(
                start = MissionReviewStart.DesktopCompose,
                projectServices =
                    fakeProjectServices(
                        selectedPath = "C:/work/mission-review",
                        recentProjectsStore = FailingRecentProjectsStore(),
                    ),
            )
        val selection =
            assertIs<ProjectSelectionChild>(
                root.childStack.value.active.instance,
            ).component

        selection.onChooseProjectClicked()

        val child = assertIs<ProjectSelectionChild>(root.childStack.value.active.instance)
        assertEquals(
            "Unable to remember project: storage is unavailable.",
            child.component.model.value.errorMessage,
        )
    }

    @Test
    fun localReviewShowsLoadingUntilInspectionCompletes() {
        val inspectionExecutor = QueuedProjectInspectionExecutor()
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "C:/work/mission-review",
                ),
                projectServices =
                    fakeProjectServices(
                        projectInspectionExecutor = inspectionExecutor,
                    ),
            )
        val review =
            assertIs<LocalReviewChild>(
                root.childStack.value.active.instance,
            ).component

        assertTrue(review.model.value.isLoading)

        inspectionExecutor.completeNext()

        assertFalse(review.model.value.isLoading)
        assertEquals("mission-review", review.model.value.title)
        assertEquals("src/Main.kt", review.model.value.selectedChangePath)
    }

    @Test
    fun localReviewCanSelectChangedFile() {
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "C:/work/mission-review",
                ),
                projectServices = fakeProjectServices(),
            )
        val review =
            assertIs<LocalReviewChild>(
                root.childStack.value.active.instance,
            ).component

        review.onChangedFileClicked("README.md")

        assertEquals("README.md", review.model.value.selectedChangePath)
        assertEquals("diff for README.md", review.model.value.diffText)
    }

    @Test
    fun localReviewRefreshReloadsProjectTreeChangesAndDiff() {
        val projectInspector = RefreshableProjectInspector()
        val root =
            createRoot(
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = "C:/work/mission-review",
                ),
                projectServices =
                    MissionReviewProjectServices(
                        projectInspector = projectInspector,
                    ),
            )
        val review =
            assertIs<LocalReviewChild>(
                root.childStack.value.active.instance,
            ).component

        review.onChangedFileClicked("README.md")
        projectInspector.includeExternalChanges = true
        review.onRefreshClicked()

        assertEquals("README.md", review.model.value.selectedChangePath)
        assertEquals(
            listOf("src/Main.kt", "README.md", "src/Generated.kt"),
            review.model.value.changedFiles.map(ProjectChange::path),
        )
        assertEquals("refreshed diff for README.md", review.model.value.diffText)
        assertTrue(
            review.model.value.fileTree
                .flatMap(ProjectFileNode::children)
                .any { node -> node.path == "src/Generated.kt" },
        )
    }

    private fun createRoot(
        start: MissionReviewStart,
        projectServices: MissionReviewProjectServices = MissionReviewProjectServices(),
    ): MissionReviewRootComponent =
        DefaultMissionReviewRootComponent(
            componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
            start = start,
            projectServices = projectServices,
        )

    private fun fakeProjectServices(
        selectedPath: String? = null,
        recentProjectsStore: RecentProjectsStore = FakeRecentProjectsStore(),
        projectInspectionExecutor: ProjectInspectionExecutor = ImmediateProjectInspectionExecutor,
    ): MissionReviewProjectServices =
        MissionReviewProjectServices(
            directoryPicker = ProjectDirectoryPicker { selectedPath },
            recentProjectsStore = recentProjectsStore,
            projectInspector = FakeProjectInspector(),
            projectInspectionExecutor = projectInspectionExecutor,
        )

    private class FakeRecentProjectsStore(
        initialProjects: List<RecentProject> = emptyList(),
    ) : RecentProjectsStore {

        private var projects = initialProjects

        override fun loadRecentProjects(): List<RecentProject> = projects

        override fun rememberProject(path: String): List<RecentProject> {
            val project =
                RecentProject(
                    path = path,
                    name = path.substringAfterLast('/'),
                    lastOpenedAtEpochMillis = projects.size.toLong() + 1L,
                )
            projects = listOf(project) + projects.filterNot { it.path == path }

            return projects
        }
    }

    private class FakeProjectInspector : ProjectInspector {

        override fun inspectProject(
            projectPath: String,
            selectedChangePath: String?,
        ): ProjectInspection {
            val selectedPath = selectedChangePath ?: "src/Main.kt"
            return ProjectInspection(
                projectPath = projectPath,
                projectName = "mission-review",
                fileTree =
                    listOf(
                        ProjectFileNode(
                            name = "src",
                            path = "src",
                            isDirectory = true,
                            children =
                                listOf(
                                    ProjectFileNode(
                                        name = "Main.kt",
                                        path = "src/Main.kt",
                                        isDirectory = false,
                                    ),
                                ),
                        ),
                    ),
                changedFiles =
                    listOf(
                        ProjectChange(
                            path = "src/Main.kt",
                            statusCode = " M",
                            statusLabel = "Modified",
                        ),
                        ProjectChange(
                            path = "README.md",
                            statusCode = " M",
                            statusLabel = "Modified",
                        ),
                    ),
                selectedChangePath = selectedPath,
                diffText = "diff for $selectedPath",
            )
        }
    }

    private class FailingRecentProjectsStore : RecentProjectsStore {

        override fun loadRecentProjects(): List<RecentProject> = emptyList()

        override fun rememberProject(path: String): List<RecentProject> {
            throw IllegalStateException("storage is unavailable")
        }
    }

    private class QueuedProjectInspectionExecutor : ProjectInspectionExecutor {

        private var pendingRequest: PendingRequest? = null

        override fun execute(
            operation: () -> ProjectInspection,
            onResult: (Result<ProjectInspection>) -> Unit,
        ): ProjectInspectionTask {
            val request =
                PendingRequest(
                    operation = operation,
                    onResult = onResult,
                )
            pendingRequest = request

            return ProjectInspectionTask {
                request.cancelled = true
            }
        }

        fun completeNext() {
            val request = requireNotNull(pendingRequest)
            pendingRequest = null
            if (!request.cancelled) {
                request.onResult(runCatching(request.operation))
            }
        }

        private class PendingRequest(
            val operation: () -> ProjectInspection,
            val onResult: (Result<ProjectInspection>) -> Unit,
        ) {
            var cancelled = false
        }
    }

    private class RefreshableProjectInspector : ProjectInspector {

        var includeExternalChanges = false

        override fun inspectProject(
            projectPath: String,
            selectedChangePath: String?,
        ): ProjectInspection {
            val changedFiles =
                listOf(
                    ProjectChange(
                        path = "src/Main.kt",
                        statusCode = " M",
                        statusLabel = "Modified",
                    ),
                    ProjectChange(
                        path = "README.md",
                        statusCode = " M",
                        statusLabel = "Modified",
                    ),
                ) +
                    if (includeExternalChanges) {
                        listOf(
                            ProjectChange(
                                path = "src/Generated.kt",
                                statusCode = "??",
                                statusLabel = "New",
                            ),
                        )
                    } else {
                        emptyList()
                    }
            val selectedPath =
                selectedChangePath
                    ?.takeIf { path -> changedFiles.any { change -> change.path == path } }
                    ?: changedFiles.first().path

            return ProjectInspection(
                projectPath = projectPath,
                projectName = "mission-review",
                fileTree =
                    listOf(
                        ProjectFileNode(
                            name = "src",
                            path = "src",
                            isDirectory = true,
                            children =
                                listOf(
                                    ProjectFileNode(
                                        name = "Main.kt",
                                        path = "src/Main.kt",
                                        isDirectory = false,
                                    ),
                                ) +
                                    if (includeExternalChanges) {
                                        listOf(
                                            ProjectFileNode(
                                                name = "Generated.kt",
                                                path = "src/Generated.kt",
                                                isDirectory = false,
                                            ),
                                        )
                                    } else {
                                        emptyList()
                                    },
                        ),
                    ),
                changedFiles = changedFiles,
                selectedChangePath = selectedPath,
                diffText =
                    if (includeExternalChanges) {
                        "refreshed diff for $selectedPath"
                    } else {
                        "diff for $selectedPath"
                    },
            )
        }
    }
}
