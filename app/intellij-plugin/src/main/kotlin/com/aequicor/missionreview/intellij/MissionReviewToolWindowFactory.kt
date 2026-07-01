package com.aequicor.missionreview.intellij

import com.aequicor.missionreview.core.git.JvmGitProjectInspector
import com.aequicor.missionreview.core.navigation.DefaultMissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.JvmProjectInspectionExecutor
import com.aequicor.missionreview.core.navigation.MissionReviewProjectServices
import com.aequicor.missionreview.core.navigation.MissionReviewStart
import com.aequicor.missionreview.ui.intellij.MissionReviewIntellijPanel
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.SwingUtilities

/**
 * Creates the Local review tool window.
 */
class MissionReviewToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val lifecycle = LifecycleRegistry()
        val projectInspectionExecutor =
            JvmProjectInspectionExecutor(
                resultDispatcher = { block -> SwingUtilities.invokeLater { block() } },
            )
        val root =
            DefaultMissionReviewRootComponent(
                componentContext = DefaultComponentContext(lifecycle = lifecycle),
                start = MissionReviewStart.IntelliJPlatform(
                    projectPath = project.basePath.orEmpty(),
                ),
                projectServices =
                    MissionReviewProjectServices(
                        projectInspector = JvmGitProjectInspector(),
                        projectInspectionExecutor = projectInspectionExecutor,
                    ),
            )
        val panel = MissionReviewIntellijPanel(root)
        val disposable = Disposer.newDisposable("mission-review Local review")

        lifecycle.resume()

        Disposer.register(disposable) {
            projectInspectionExecutor.close()
            panel.dispose()
            lifecycle.destroy()
        }

        val content = ContentFactory.getInstance().createContent(panel, "Local review", false)
        content.setDisposer(disposable)
        toolWindow.contentManager.addContent(content)
    }
}
