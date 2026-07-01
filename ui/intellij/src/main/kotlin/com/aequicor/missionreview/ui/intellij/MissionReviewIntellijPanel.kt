package com.aequicor.missionreview.ui.intellij

import com.aequicor.missionreview.core.navigation.LocalReviewComponent
import com.aequicor.missionreview.core.navigation.LocalReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.ProjectSelectionChild
import com.aequicor.missionreview.core.navigation.ProjectSelectionComponent
import com.arkivanov.decompose.Cancellation
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Swing adapter for rendering the shared navigation root inside IntelliJ Platform.
 *
 * [component] is created by the IntelliJ plugin app layer, which owns project
 * context and Decompose lifecycle integration.
 */
class MissionReviewIntellijPanel(
    private val component: MissionReviewRootComponent,
) : JPanel(BorderLayout()) {

    private var disposed = false
    private var stackCancellation: Cancellation? = null

    init {
        border = BorderFactory.createEmptyBorder(16, 16, 16, 16)
        stackCancellation = component.childStack.subscribe { stack ->
            renderChild(stack.active.instance)
        }
    }

    fun dispose() {
        disposed = true
        stackCancellation?.cancel()
        stackCancellation = null
        removeAll()
    }

    private fun renderChild(child: MissionReviewChild) {
        if (disposed) {
            return
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater { renderChild(child) }
            return
        }

        removeAll()
        add(
            when (child) {
                is ProjectSelectionChild ->
                    projectSelectionPanel(child.component)

                is LocalReviewChild ->
                    localReviewPanel(child.component)
            },
            BorderLayout.CENTER,
        )
        revalidate()
        repaint()
    }

    private fun localReviewPanel(component: LocalReviewComponent): JComponent {
        val model = component.model.value
        val panel = verticalPanel()

        val header = JPanel(BorderLayout()).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            add(titleBlock(title = model.title, subtitle = model.description), BorderLayout.CENTER)

            if (model.canNavigateBack) {
                add(
                    JButton("Back").apply {
                        addActionListener { component.onBackClicked() }
                    },
                    BorderLayout.EAST,
                )
            }
        }

        panel.add(header)

        return panel
    }

    private fun projectSelectionPanel(component: ProjectSelectionComponent): JComponent {
        val model = component.model.value

        return verticalPanel().apply {
            add(titleBlock(title = model.title, subtitle = model.description))
            add(Box.createVerticalStrut(16))
            add(
                JButton("Open review placeholder").apply {
                    alignmentX = Component.LEFT_ALIGNMENT
                    addActionListener { component.onOpenProjectClicked() }
                },
            )
        }
    }

    private fun titleBlock(
        title: String,
        subtitle: String,
    ): JComponent =
        verticalPanel().apply {
            add(JLabel(title).apply { alignmentX = Component.LEFT_ALIGNMENT })
            add(Box.createVerticalStrut(4))
            add(JLabel(subtitle).apply {
                alignmentX = Component.LEFT_ALIGNMENT
                foreground = Color(0x66, 0x66, 0x66)
            })
        }

    private fun verticalPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
        }
}
