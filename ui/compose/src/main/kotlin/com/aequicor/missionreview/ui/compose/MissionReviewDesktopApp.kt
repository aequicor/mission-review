package com.aequicor.missionreview.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.aequicor.missionreview.core.navigation.MissionReviewChild
import com.aequicor.missionreview.core.navigation.MissionReviewRootComponent
import com.aequicor.missionreview.feature.entrypoint.api.EntrypointComponent
import com.aequicor.missionreview.feature.review.api.ReviewComponent

@Composable
fun MissionReviewDesktopApp(rootComponent: MissionReviewRootComponent) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val child = rootComponent.child.value) {
                is MissionReviewChild.Entrypoint -> EntrypointScreen(child.component)
                is MissionReviewChild.Review -> ReviewScreen(child.component)
            }
        }
    }
}

@Composable
private fun EntrypointScreen(component: EntrypointComponent) {
    val state = component.state.value

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("mission-review", style = MaterialTheme.typography.h5)
        OutlinedTextField(
            value = state.projectPath,
            onValueChange = component::selectProject,
            label = { Text("Project path") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row {
            Button(
                onClick = component::openSelectedProject,
                enabled = state.canOpenReview,
            ) {
                Text("Open local review")
            }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colors.error) }
    }
}

@Composable
private fun ReviewScreen(component: ReviewComponent) {
    val state = component.state.value
    val clipboardManager = LocalClipboardManager.current
    var commentBody by remember { mutableStateOf("") }
    var required by remember { mutableStateOf(true) }
    val selectedFile = state.files.firstOrNull { it.path == state.selectedFilePath }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = component::reload) {
                Text("Reload")
            }
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(component.exportCommentaries()))
                },
            ) {
                Text("Copy commentaries to clipboard")
            }
            Button(onClick = { component.saveThroughMcp() }) {
                Text("Save through MCP")
            }
        }

        Text(state.projectRoot, style = MaterialTheme.typography.subtitle2)
        state.errorMessage?.let { Text(it, color = MaterialTheme.colors.error) }

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(0.35f)) {
                Text("Files", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                state.files.forEach { file ->
                    Button(
                        onClick = { component.selectFile(file.path) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("${file.status}: ${file.path}")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(0.65f)) {
                Text(selectedFile?.path ?: "No file selected", style = MaterialTheme.typography.subtitle1)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                selectedFile?.lines.orEmpty().take(80).forEach { line ->
                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.caption,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = commentBody,
                    onValueChange = { commentBody = it },
                    label = { Text("Review comment") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    Checkbox(checked = required, onCheckedChange = { required = it })
                    Text("Required fix")
                }
                Button(
                    onClick = {
                        val filePath = selectedFile?.path ?: return@Button
                        component.addComment(filePath, line = null, body = commentBody, required = required)
                        commentBody = ""
                    },
                    enabled = selectedFile != null && commentBody.isNotBlank(),
                ) {
                    Text("Add comment")
                }
            }
        }
    }
}
