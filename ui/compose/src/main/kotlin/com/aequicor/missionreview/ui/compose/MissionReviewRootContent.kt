package com.aequicor.missionreview.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aequicor.missionreview.core.navigation.LocalReviewComponent
import com.aequicor.missionreview.core.navigation.LocalReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.core.navigation.ProjectSelectionChild
import com.aequicor.missionreview.core.navigation.ProjectSelectionComponent
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

/**
 * Renders the shared mission-review root component in Compose Desktop.
 *
 * The composable is target UI only: navigation state stays in [component], and
 * this function only maps active children to placeholder screens.
 */
@Composable
fun MissionReviewRootContent(
    component: MissionReviewRootComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.childStack,
        modifier = modifier,
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

@Composable
private fun ProjectSelectionContent(
    component: ProjectSelectionComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceholderHeader(
                title = model.title,
                description = model.description,
            )

            Button(onClick = component::onOpenProjectClicked) {
                Text("Open review placeholder")
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

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceholderHeader(
                title = model.title,
                description = model.description,
            )

            if (model.canNavigateBack) {
                Button(onClick = component::onBackClicked) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderHeader(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h5,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f),
        )
    }
}
