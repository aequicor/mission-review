package com.aequicor.missionreview.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aequicor.missionreview.core.git.ProjectChange
import com.aequicor.missionreview.core.git.ProjectFileNode
import com.aequicor.missionreview.core.navigation.LocalReviewComponent
import com.aequicor.missionreview.core.navigation.LocalReviewChild
import com.aequicor.missionreview.core.navigation.LocalReviewModel
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.ProjectSelectionChild
import com.aequicor.missionreview.core.navigation.ProjectSelectionComponent
import com.aequicor.missionreview.core.navigation.ProjectSelectionModel
import com.aequicor.missionreview.core.storage.RecentProject
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Renders the shared mission-review root component in Compose Desktop.
 *
 * The composable is target UI only: navigation state stays in [component], and
 * this function maps active children to desktop screens.
 */
@Composable
fun MissionReviewRootContent(
    component: MissionReviewRootComponent,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MissionReviewColors.AppBackground,
    ) {
        Children(
            stack = component.childStack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(fade()),
        ) { child ->
            when (val instance = child.instance) {
                is ProjectSelectionChild ->
                    ProjectSelectionContent(
                        component = instance.component,
                        modifier = Modifier.fillMaxSize(),
                    )

                is LocalReviewChild ->
                    LocalReviewContent(
                        component = instance.component,
                        modifier = Modifier.fillMaxSize(),
                    )
            }
        }
    }
}

@Composable
private fun ProjectSelectionContent(
    component: ProjectSelectionComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Column(
        modifier =
            modifier.padding(
                start = 48.dp,
                top = 82.dp,
                end = 48.dp,
                bottom = 48.dp,
            ),
    ) {
        ProjectSelectionHeader(
            model = model,
            onChooseProjectClicked = component::onChooseProjectClicked,
        )

        model.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(18.dp))
            ErrorMessage(
                message = message,
                onDismissClicked = component::onDismissErrorClicked,
            )
        }

        Spacer(modifier = Modifier.height(90.dp))

        Text(
            text = "Recent projects",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.SemiBold,
            color = MissionReviewColors.TextPrimary,
        )

        Spacer(modifier = Modifier.height(22.dp))

        RecentProjectsPanel(
            recentProjects = model.recentProjects,
            onRecentProjectClicked = component::onRecentProjectClicked,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun ProjectSelectionHeader(
    model: ProjectSelectionModel,
    onChooseProjectClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.SemiBold,
                color = MissionReviewColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = model.description,
                style = MaterialTheme.typography.h6,
                color = MissionReviewColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        PrimaryActionButton(
            text = "Choose project",
            onClick = onChooseProjectClicked,
        )
    }
}

@Composable
private fun RecentProjectsPanel(
    recentProjects: List<RecentProject>,
    onRecentProjectClicked: (RecentProject) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MissionReviewColors.Surface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MissionReviewColors.Border),
    ) {
        if (recentProjects.isEmpty()) {
            EmptyRecentProjectsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                recentProjects.forEachIndexed { index, project ->
                    if (index > 0) {
                        Divider(color = MissionReviewColors.BorderSoft)
                    }
                    RecentProjectRow(
                        project = project,
                        onClick = { onRecentProjectClicked(project) },
                    )
                    Divider(color = MissionReviewColors.BorderSoft)
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentProjectsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FolderIcon(
                modifier = Modifier.size(52.dp),
                color = MissionReviewColors.TextMuted,
                fill = Color.Transparent,
            )
            Text(
                text = "No recent projects yet.",
                style = MaterialTheme.typography.body1,
                color = MissionReviewColors.TextMuted,
            )
        }
    }
}

@Composable
private fun RecentProjectRow(
    project: RecentProject,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderIcon(
            modifier = Modifier.size(54.dp),
            color = MissionReviewColors.Lapis,
            fill = Color.Transparent,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.h6,
                color = MissionReviewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = project.path,
                style = MaterialTheme.typography.body2,
                color = MissionReviewColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BranchIcon(
                    modifier = Modifier.size(22.dp),
                    color = MissionReviewColors.Lapis,
                )
                Text(
                    text = "Git project",
                    style = MaterialTheme.typography.body1,
                    color = MissionReviewColors.TextPrimary,
                    maxLines = 1,
                )
            }

            Divider(
                color = MissionReviewColors.Border,
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClockIcon(
                    modifier = Modifier.size(22.dp),
                    color = MissionReviewColors.TextSecondary,
                )
                Text(
                    text = formatRecentOpenedAt(project.lastOpenedAtEpochMillis),
                    style = MaterialTheme.typography.body1,
                    color = MissionReviewColors.TextSecondary,
                    maxLines = 1,
                )
            }

            ChevronRightIcon(
                modifier = Modifier.size(24.dp),
                color = MissionReviewColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismissClicked: () -> Unit,
) {
    Surface(
        color = MissionReviewColors.ErrorSoft,
        contentColor = MissionReviewColors.Error,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MissionReviewColors.Error.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2,
            )
            TextButton(onClick = onDismissClicked) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun LocalReviewContent(
    component: LocalReviewComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Column(modifier = modifier.background(MissionReviewColors.Surface)) {
        ReviewTopBar(
            model = model,
            onBackClicked = component::onBackClicked,
            onRefreshClicked = component::onRefreshClicked,
        )

        Divider(color = MissionReviewColors.Border)

        Row(modifier = Modifier.fillMaxSize()) {
            ProjectSidebar(
                model = model,
                onChangedFileClicked = component::onChangedFileClicked,
                onRefreshClicked = component::onRefreshClicked,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp),
            )

            Divider(
                color = MissionReviewColors.Border,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
            )

            DiffPanel(
                model = model,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReviewTopBar(
    model: LocalReviewModel,
    onBackClicked: () -> Unit,
    onRefreshClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(MissionReviewColors.Surface)
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold,
                color = MissionReviewColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = model.projectPath,
                style = MaterialTheme.typography.caption,
                color = MissionReviewColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        TopBarAction(
            text = "Refresh",
            symbol = "↻",
            onClick = onRefreshClicked,
        )

        if (model.canNavigateBack) {
            Divider(
                color = MissionReviewColors.Border,
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
            )
            TopBarAction(
                text = "Back",
                symbol = "←",
                onClick = onBackClicked,
            )
        }
    }
}

@Composable
private fun TopBarAction(
    text: String,
    symbol: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.h6,
            color = MissionReviewColors.Lapis,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = MissionReviewColors.TextPrimary,
        )
    }
}

@Composable
private fun ProjectSidebar(
    model: LocalReviewModel,
    onChangedFileClicked: (String) -> Unit,
    onRefreshClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedChange = model.changedFiles.firstOrNull { change -> change.path == model.selectedChangePath }

    Column(
        modifier = modifier
            .background(MissionReviewColors.Surface)
            .padding(start = 22.dp, top = 18.dp, end = 18.dp, bottom = 18.dp),
    ) {
        SidebarTitleRow(title = "Project files")
        Divider(color = MissionReviewColors.Border, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.28f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (model.fileTree.isEmpty()) {
                SecondaryText(if (model.isLoading) "Loading files." else "No files to show.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    model.fileTree.forEach { node ->
                        FileNodeRow(
                            node = node,
                            selectedChange = selectedChange,
                        )
                    }
                }
            }
        }

        Divider(color = MissionReviewColors.Border, modifier = Modifier.padding(top = 14.dp, bottom = 12.dp))

        SidebarTitleRow(
            title = "Changed files",
            trailingContent = {
                SidebarRefreshButton(onClick = onRefreshClicked)
            },
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (model.changedFiles.isEmpty()) {
                SecondaryText(if (model.isLoading) "Loading changes." else "No changed files.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    changeGroups(model.changedFiles).forEach { group ->
                        ChangeGroupBlock(
                            group = group,
                            selectedPath = model.selectedChangePath,
                            onChangedFileClicked = onChangedFileClicked,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarTitleRow(
    title: String,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            color = MissionReviewColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        trailingContent?.invoke()
    }
}

@Composable
private fun SidebarRefreshButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RefreshIcon(
            modifier = Modifier.size(16.dp),
            color = MissionReviewColors.TextMuted,
        )
    }
}

@Composable
private fun FileNodeRow(
    node: ProjectFileNode,
    selectedChange: ProjectChange?,
    depth: Int = 0,
) {
    val selected = node.path == selectedChange?.path

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (selected) MissionReviewColors.LapisSelected else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(start = (depth * 10).dp + 2.dp, top = 4.dp, end = 6.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (node.isDirectory) {
                FolderIcon(
                    modifier = Modifier.size(15.dp),
                    color = MissionReviewColors.TextSecondary,
                    fill = Color.Transparent,
                )
            } else {
                FileIcon(
                    modifier = Modifier.size(15.dp),
                    color = if (selected) MissionReviewColors.Lapis else MissionReviewColors.TextSecondary,
                )
            }

            Text(
                text = node.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2,
                color = if (selected) MissionReviewColors.Lapis else MissionReviewColors.TextPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            selectedChange?.let { change ->
                if (selected) {
                    val badgeColor = statusAccentFor(change.statusLabel)
                    ChangeInitialBadge(
                        label = statusInitialFor(change.statusLabel),
                        color = badgeColor,
                        background = badgeColor,
                        content = Color.White,
                    )
                }
            }
        }

        node.children.forEach { child ->
            FileNodeRow(
                node = child,
                selectedChange = selectedChange,
                depth = depth + 1,
            )
        }
    }
}

@Composable
private fun ChangeGroupBlock(
    group: ChangeGroup,
    selectedPath: String?,
    onChangedFileClicked: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                color = group.color,
                shape = RoundedCornerShape(4.dp),
                content = {},
            )
            Text(
                text = "${group.title} (${group.changes.size})",
                style = MaterialTheme.typography.body2,
                color = MissionReviewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        group.changes.forEach { change ->
            ChangedFileRow(
                change = change,
                selected = change.path == selectedPath,
                onClick = { onChangedFileClicked(change.path) },
            )
        }
    }
}

@Composable
private fun ChangedFileRow(
    change: ProjectChange,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background =
        if (selected) {
            MissionReviewColors.LapisSelected
        } else {
            Color.Transparent
        }
    val border =
        if (selected) {
            MissionReviewColors.Lapis.copy(alpha = 0.18f)
        } else {
            Color.Transparent
        }
    val badgeColor = statusAccentFor(change.statusLabel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(2.dp))
            .border(
                width = 1.dp,
                color = border,
                shape = RoundedCornerShape(2.dp),
            )
            .clickable(onClick = onClick)
            .padding(start = 22.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = change.path,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.caption,
            color = MissionReviewColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        ChangeInitialBadge(
            label = statusInitialFor(change.statusLabel),
            color = badgeColor,
            background = badgeColor,
            content = Color.White,
        )
    }
}

@Composable
private fun DiffPanel(
    model: LocalReviewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MissionReviewColors.Surface)
            .padding(start = 18.dp, top = 18.dp, end = 0.dp, bottom = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = model.selectedChangePath ?: "Project changes",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.SemiBold,
                    color = MissionReviewColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.body2,
                    color = MissionReviewColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MissionReviewColors.CodeBackground,
            shape = RoundedCornerShape(topStart = 4.dp),
            border = BorderStroke(1.dp, MissionReviewColors.Border),
        ) {
            Column {
                Text(
                    text = if (model.isLoading) "Loading" else diffHeadingFor(model.diffText),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
                    style = MaterialTheme.typography.body2,
                    color = MissionReviewColors.Lapis,
                    fontWeight = FontWeight.SemiBold,
                )

                Divider(color = MissionReviewColors.Border)

                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = displayDiffText(model.diffText.ifBlank { model.description }),
                            style = MaterialTheme.typography.body2,
                            color = MissionReviewColors.CodeText,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .width(220.dp)
                .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        colors =
            ButtonDefaults.buttonColors(
                backgroundColor = MissionReviewColors.Lapis,
                contentColor = Color.White,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FolderIcon(
                modifier = Modifier.size(28.dp),
                color = Color.White,
                fill = Color.Transparent,
            )
        }
    }
}

@Composable
private fun ChangeInitialBadge(
    label: String,
    color: Color,
    background: Color,
    content: Color,
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(3.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.caption.copy(letterSpacing = 0.sp),
            color = content,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun SecondaryText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        color = MissionReviewColors.TextMuted,
    )
}

@Composable
private fun FolderIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.Lapis,
    fill: Color = MissionReviewColors.LapisSoft,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val tabHeight = size.height * 0.28f
        val bodyTop = size.height * 0.24f
        val radius = 3.dp.toPx()

        drawRoundRect(
            color = fill,
            topLeft = Offset(x = size.width * 0.05f, y = bodyTop),
            size = Size(width = size.width * 0.9f, height = size.height * 0.66f),
            cornerRadius = CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(x = size.width * 0.05f, y = bodyTop),
            size = Size(width = size.width * 0.9f, height = size.height * 0.66f),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.08f, bodyTop),
            end = Offset(size.width * 0.35f, bodyTop),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.13f, tabHeight),
            end = Offset(size.width * 0.38f, tabHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, tabHeight),
            end = Offset(size.width * 0.48f, bodyTop),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun FileIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.TextSecondary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.5.dp.toPx()
        val radius = 2.dp.toPx()
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(x = size.width * 0.18f, y = size.height * 0.08f),
            size = Size(width = size.width * 0.62f, height = size.height * 0.84f),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.32f, size.height * 0.56f),
            end = Offset(size.width * 0.67f, size.height * 0.56f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun BranchIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.Lapis,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val nodeRadius = size.minDimension * 0.12f
        val top = Offset(size.width * 0.28f, size.height * 0.22f)
        val bottom = Offset(size.width * 0.28f, size.height * 0.78f)
        val branch = Offset(size.width * 0.72f, size.height * 0.36f)

        drawLine(
            color = color,
            start = top,
            end = bottom,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.48f),
            end = branch,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = nodeRadius, center = top, style = Stroke(width = strokeWidth))
        drawCircle(color = color, radius = nodeRadius, center = bottom, style = Stroke(width = strokeWidth))
        drawCircle(color = color, radius = nodeRadius, center = branch, style = Stroke(width = strokeWidth))
    }
}

@Composable
private fun ClockIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.TextSecondary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.38f

        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = strokeWidth))
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - radius * 0.52f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x + radius * 0.48f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.TextSecondary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.24f),
            end = Offset(size.width * 0.64f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun RefreshIcon(
    modifier: Modifier = Modifier,
    color: Color = MissionReviewColors.TextSecondary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.7.dp.toPx()
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = color,
            startAngle = -35f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.76f, size.height * 0.12f),
            end = Offset(size.width * 0.88f, size.height * 0.34f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.76f, size.height * 0.12f),
            end = Offset(size.width * 0.52f, size.height * 0.18f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun changeGroups(changes: List<ProjectChange>): List<ChangeGroup> {
    val order = listOf("Modified", "New", "Deleted", "Renamed", "Copied")
    val groupedChanges = changes.groupBy { change -> change.statusLabel }
    val orderedGroups =
        order.mapNotNull { label ->
            groupedChanges[label]?.let { groupChanges ->
                ChangeGroup(
                    title = label,
                    color = statusAccentFor(label),
                    changes = groupChanges,
                )
            }
        }
    val otherGroups =
        groupedChanges
            .filterKeys { label -> label !in order }
            .toSortedMap()
            .map { (label, groupChanges) ->
                ChangeGroup(
                    title = label,
                    color = statusAccentFor(label),
                    changes = groupChanges,
                )
            }

    return orderedGroups + otherGroups
}

private fun statusInitialFor(statusLabel: String): String =
    when (statusLabel) {
        "New" -> "N"
        "Modified" -> "M"
        "Deleted" -> "D"
        "Renamed" -> "R"
        "Copied" -> "C"
        else -> statusLabel.firstOrNull()?.uppercase().orEmpty()
    }

private fun statusAccentFor(statusLabel: String): Color =
    when (statusLabel) {
        "New" -> MissionReviewColors.New
        "Deleted" -> MissionReviewColors.Error
        "Renamed", "Copied" -> MissionReviewColors.Cyan
        else -> MissionReviewColors.Lapis
    }

private fun diffHeadingFor(diffText: String): String =
    when {
        diffText.startsWith("Staged changes") -> "Staged changes"
        diffText.startsWith("Untracked file preview") -> "New file preview"
        diffText.startsWith("No ") -> "Diff preview"
        else -> "Unstaged changes"
    }

private fun displayDiffText(diffText: String): String =
    diffText
        .removePrefix("Staged changes\n\n")
        .removePrefix("Unstaged changes\n\n")
        .removePrefix("Untracked file preview\n\n")

private fun formatRecentOpenedAt(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val openedAt = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val time = openedAt.format(DateTimeFormatter.ofPattern("HH:mm"))

    return if (openedAt.toLocalDate() == LocalDate.now(zone)) {
        "Today, $time"
    } else {
        openedAt.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }
}

private data class ChangeGroup(
    val title: String,
    val color: Color,
    val changes: List<ProjectChange>,
)

private object MissionReviewColors {
    val AppBackground = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val CodeBackground = Color(0xFFFDFEFF)
    val Border = Color(0xFFD9DEE4)
    val BorderSoft = Color(0xFFE9EDF2)
    val Lapis = Color(0xFF26619C)
    val LapisSelected = Color(0xFFE9F3FF)
    val LapisSoft = Color(0xFFE8F1F8)
    val Cyan = Color(0xFF137F98)
    val New = Color(0xFFE59A00)
    val Error = Color(0xFFB42318)
    val ErrorSoft = Color(0xFFFFEDEA)
    val TextPrimary = Color(0xFF1B2733)
    val TextSecondary = Color(0xFF5F6F7D)
    val TextMuted = Color(0xFF8793A0)
    val CodeText = Color(0xFF1F2933)
}
