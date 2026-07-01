package com.aequicor.missionreview.ui.intellij

import com.aequicor.missionreview.core.git.ProjectChange
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
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
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
    private var modelCancellation: Cancellation? = null

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
        modelCancellation?.cancel()
        modelCancellation = null
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

        modelCancellation?.cancel()
        modelCancellation = null

        removeAll()
        add(
            when (child) {
                is ProjectSelectionChild ->
                    projectSelectionPanel(child.component)

                is LocalReviewChild -> {
                    subscribeToLocalReviewModel(child.component)
                    localReviewPanel(child.component)
                }
            },
            BorderLayout.CENTER,
        )
        revalidate()
        repaint()
    }

    private fun localReviewPanel(component: LocalReviewComponent): JComponent {
        val model = component.model.value
        val rootPanel = JPanel(BorderLayout(12, 12))

        val header = JPanel(BorderLayout()).apply {
            add(titleBlock(title = model.title, subtitle = model.description), BorderLayout.CENTER)
            val actions = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                add(
                    JButton("Refresh").apply {
                        addActionListener {
                            component.onRefreshClicked()
                        }
                    },
                )
                if (model.canNavigateBack) {
                    add(Box.createHorizontalStrut(8))
                    add(
                        JButton("Back").apply {
                            addActionListener { component.onBackClicked() }
                        },
                    )
                }
            }
            add(actions, BorderLayout.EAST)
        }

        val diffArea =
            JTextArea(model.diffText.ifBlank { model.description }).apply {
                isEditable = false
                lineWrap = false
                font = font.deriveFont(12f)
            }

        val changesList = changedFilesList(
            changes = model.changedFiles,
            selectedPath = model.selectedChangePath,
            onSelected = { change ->
                component.onChangedFileClicked(change.path)
            },
        )

        val splitPane =
            JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                JScrollPane(changesList).apply {
                    minimumSize = Dimension(260, 200)
                    preferredSize = Dimension(320, 600)
                },
                JScrollPane(diffArea),
            ).apply {
                resizeWeight = 0.24
                border = BorderFactory.createEmptyBorder()
            }

        rootPanel.add(header, BorderLayout.NORTH)
        rootPanel.add(splitPane, BorderLayout.CENTER)

        return rootPanel
    }

    private fun subscribeToLocalReviewModel(component: LocalReviewComponent) {
        var firstEmission = true
        modelCancellation = component.model.subscribe {
            if (firstEmission) {
                firstEmission = false
            } else {
                renderChild(LocalReviewChild(component))
            }
        }
    }

    private fun changedFilesList(
        changes: List<ProjectChange>,
        selectedPath: String?,
        onSelected: (ProjectChange) -> Unit,
    ): JList<ProjectChange> {
        val listModel = DefaultListModel<ProjectChange>()
        changes.forEach(listModel::addElement)

        return JList(listModel).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer = ProjectChangeListCellRenderer()
            val selectedIndex = changes.indexOfFirst { it.path == selectedPath }.takeIf { it >= 0 } ?: 0
            if (changes.isNotEmpty()) {
                this.selectedIndex = selectedIndex
            }
            addListSelectionListener { event ->
                if (!event.valueIsAdjusting) {
                    selectedValue?.let(onSelected)
                }
            }
        }
    }

    private fun projectSelectionPanel(component: ProjectSelectionComponent): JComponent {
        val model = component.model.value

        return verticalPanel().apply {
            add(titleBlock(title = model.title, subtitle = model.description))
            add(Box.createVerticalStrut(16))
            add(
                JButton("Choose project").apply {
                    alignmentX = Component.LEFT_ALIGNMENT
                    addActionListener { component.onChooseProjectClicked() }
                },
            )

            if (model.recentProjects.isNotEmpty()) {
                add(Box.createVerticalStrut(16))
                add(JLabel("Recent projects").apply { alignmentX = Component.LEFT_ALIGNMENT })
                model.recentProjects.forEach { project ->
                    add(Box.createVerticalStrut(6))
                    add(
                        JButton(project.name).apply {
                            alignmentX = Component.LEFT_ALIGNMENT
                            toolTipText = project.path
                            addActionListener { component.onRecentProjectClicked(project) }
                        },
                    )
                }
            }
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
