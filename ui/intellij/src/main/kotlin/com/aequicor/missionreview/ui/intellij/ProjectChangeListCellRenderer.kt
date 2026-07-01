package com.aequicor.missionreview.ui.intellij

import com.aequicor.missionreview.core.git.ProjectChange
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

internal class ProjectChangeListCellRenderer : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        val component =
            super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus,
            )

        if (value is ProjectChange) {
            text = "${value.statusLabel}  ${value.path}"
        }

        return component
    }
}
