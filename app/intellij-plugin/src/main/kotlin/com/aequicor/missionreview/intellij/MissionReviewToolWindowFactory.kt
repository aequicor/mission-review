package com.aequicor.missionreview.intellij

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Creates the placeholder Local review tool window.
 */
class MissionReviewToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())
        val label = JBLabel("mission-review placeholder").apply {
            horizontalAlignment = SwingConstants.CENTER
        }
        panel.add(label, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(panel, "Local review", false)
        toolWindow.contentManager.addContent(content)
    }
}
