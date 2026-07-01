package com.aequicor.missionreview.core.git

import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * JVM project inspector backed by the local filesystem and `git` executable.
 */
class JvmGitProjectInspector(
    private val maxTreeDepth: Int = DEFAULT_MAX_TREE_DEPTH,
    private val maxTreeNodes: Int = DEFAULT_MAX_TREE_NODES,
    private val maxDiffChars: Int = DEFAULT_MAX_DIFF_CHARS,
) : ProjectInspector {

    override fun inspectProject(
        projectPath: String,
        selectedChangePath: String?,
    ): ProjectInspection {
        val root = Paths.get(projectPath).toAbsolutePath().normalize()
        if (!Files.isDirectory(root)) {
            return ProjectInspection(
                projectPath = root.toString(),
                projectName = root.fileName?.toString() ?: root.toString(),
                message = "Selected path is not a directory.",
            )
        }

        val fileTree = readFileTree(root)
        val gitAvailable = isGitRepository(root)
        if (!gitAvailable) {
            return ProjectInspection(
                projectPath = root.toString(),
                projectName = root.fileName?.toString() ?: root.toString(),
                fileTree = fileTree,
                message = "No Git repository found in this directory. Initialize Git or choose another project.",
            )
        }

        val changedFiles = readChangedFiles(root)
        val selectedPath =
            selectedChangePath
                ?.takeIf { selected -> changedFiles.any { it.path == selected } }
                ?: changedFiles.firstOrNull()?.path
        val diffText =
            selectedPath
                ?.let { readDiff(root = root, relativePath = it, change = changedFiles.first { change -> change.path == it }) }
                ?: "No changed file selected."

        return ProjectInspection(
            projectPath = root.toString(),
            projectName = root.fileName?.toString() ?: root.toString(),
            fileTree = fileTree,
            changedFiles = changedFiles,
            selectedChangePath = selectedPath,
            diffText = diffText,
            message = if (changedFiles.isEmpty()) "No Git changes found." else null,
        )
    }

    private fun readFileTree(root: Path): List<ProjectFileNode> {
        val remainingNodes = RemainingNodes(maxTreeNodes)
        return readDirectoryChildren(
            root = root,
            directory = root,
            depth = 0,
            remainingNodes = remainingNodes,
        )
    }

    private fun readDirectoryChildren(
        root: Path,
        directory: Path,
        depth: Int,
        remainingNodes: RemainingNodes,
    ): List<ProjectFileNode> {
        if (depth >= maxTreeDepth || remainingNodes.isExhausted) {
            return emptyList()
        }

        return runCatching {
            Files.list(directory).use { stream ->
                stream
                    .filter { path -> !shouldSkip(path) }
                    .sorted(::compareTreePaths)
                    .map { path ->
                        readFileNode(
                            root = root,
                            path = path,
                            depth = depth,
                            remainingNodes = remainingNodes,
                        )
                    }
                    .filter { node -> node != null }
                    .map { node -> node as ProjectFileNode }
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

    private fun readFileNode(
        root: Path,
        path: Path,
        depth: Int,
        remainingNodes: RemainingNodes,
    ): ProjectFileNode? {
        if (!remainingNodes.tryConsume()) {
            return null
        }

        val isDirectory = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
        return ProjectFileNode(
            name = path.fileName?.toString() ?: path.toString(),
            path = root.relativize(path).toString().replace('\\', '/'),
            isDirectory = isDirectory,
            children =
                if (isDirectory) {
                    readDirectoryChildren(
                        root = root,
                        directory = path,
                        depth = depth + 1,
                        remainingNodes = remainingNodes,
                    )
                } else {
                    emptyList()
                },
        )
    }

    private fun shouldSkip(path: Path): Boolean =
        path.fileName?.toString() in skippedNames

    private fun compareTreePaths(left: Path, right: Path): Int {
        val leftDirectory = Files.isDirectory(left, LinkOption.NOFOLLOW_LINKS)
        val rightDirectory = Files.isDirectory(right, LinkOption.NOFOLLOW_LINKS)

        return when {
            leftDirectory && !rightDirectory -> -1
            !leftDirectory && rightDirectory -> 1
            else -> left.fileName.toString().compareTo(right.fileName.toString(), ignoreCase = true)
        }
    }

    private fun isGitRepository(root: Path): Boolean =
        runGit(root, "rev-parse", "--is-inside-work-tree").exitCode == 0

    private fun readChangedFiles(root: Path): List<ProjectChange> {
        val result = runGit(root, "status", "--porcelain=v1", "--untracked-files=all")
        if (result.exitCode != 0) {
            return emptyList()
        }

        return result.output
            .lineSequence()
            .mapNotNull(::parseStatusLine)
            .sortedWith(compareBy(ProjectChange::path))
            .toList()
    }

    private fun parseStatusLine(line: String): ProjectChange? {
        if (line.length < STATUS_PREFIX_LENGTH) {
            return null
        }

        val statusCode = line.take(2)
        val rawPath = line.drop(STATUS_PREFIX_LENGTH)
        val path = rawPath.substringAfter(" -> ").trim().trim('"')
        if (path.isBlank()) {
            return null
        }

        return ProjectChange(
            path = path,
            statusCode = statusCode,
            statusLabel = statusLabelFor(statusCode),
        )
    }

    private fun statusLabelFor(statusCode: String): String =
        when {
            statusCode == "??" -> "New"
            'A' in statusCode -> "New"
            'M' in statusCode -> "Modified"
            'D' in statusCode -> "Deleted"
            'R' in statusCode -> "Renamed"
            'C' in statusCode -> "Copied"
            else -> statusCode.trim().ifBlank { "Changed" }
        }

    private fun readDiff(
        root: Path,
        relativePath: String,
        change: ProjectChange,
    ): String {
        val unstagedDiff = runGit(root, "diff", "--", relativePath).output.takeIf(String::isNotBlank)
        val stagedDiff = runGit(root, "diff", "--cached", "--", relativePath).output.takeIf(String::isNotBlank)
        val diffText =
            listOfNotNull(
                stagedDiff?.let { "Staged changes\n\n$it" },
                unstagedDiff?.let { "Unstaged changes\n\n$it" },
            ).joinToString(separator = "\n\n")

        if (diffText.isNotBlank()) {
            return diffText.limitForUi()
        }

        if (change.statusCode == "??") {
            return readUntrackedPreview(root.resolve(relativePath))
        }

        return "No textual diff is available for $relativePath."
    }

    private fun readUntrackedPreview(path: Path): String =
        runCatching {
            if (!Files.isRegularFile(path) || Files.size(path) > MAX_PREVIEW_FILE_BYTES) {
                return@runCatching "No textual preview is available for ${path.fileName}."
            }

            "Untracked file preview\n\n" + Files.readString(path).limitForUi()
        }.getOrElse {
            "No textual preview is available for ${path.fileName}."
        }

    private fun runGit(
        root: Path,
        vararg arguments: String,
    ): CommandResult {
        var outputFile: Path? = null
        var process: Process? = null
        try {
            val temporaryOutputFile = Files.createTempFile("mission-review-git-", ".out")
            outputFile = temporaryOutputFile
            val command = listOf("git", "-C", root.toString()) + arguments
            process =
                ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(temporaryOutputFile.toFile())
                    .start()
            val completed = process.waitFor(GIT_COMMAND_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            if (!completed) {
                process.destroyForcibly()
                return CommandResult(
                    exitCode = GIT_COMMAND_TIMEOUT,
                    output = "Git command timed out.",
                )
            }

            return CommandResult(
                exitCode = process.exitValue(),
                output = Files.readString(temporaryOutputFile).trimEnd(),
            )
        } catch (exception: IOException) {
            return CommandResult(
                exitCode = GIT_COMMAND_FAILED,
                output = exception.message.orEmpty(),
            )
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            process?.destroyForcibly()
            return CommandResult(
                exitCode = GIT_COMMAND_FAILED,
                output = exception.message.orEmpty(),
            )
        } finally {
            outputFile?.let { file -> runCatching { Files.deleteIfExists(file) } }
        }
    }

    private fun String.limitForUi(): String =
        if (length <= maxDiffChars) {
            this
        } else {
            take(maxDiffChars) + "\n\n[Output truncated]"
        }

    private data class CommandResult(
        val exitCode: Int,
        val output: String,
    )

    private class RemainingNodes(
        private var count: Int,
    ) {
        val isExhausted: Boolean
            get() = count <= 0

        fun tryConsume(): Boolean {
            if (isExhausted) {
                return false
            }

            count -= 1
            return true
        }
    }

    private companion object {
        private const val DEFAULT_MAX_TREE_DEPTH = 5
        private const val DEFAULT_MAX_TREE_NODES = 600
        private const val DEFAULT_MAX_DIFF_CHARS = 80_000
        private const val STATUS_PREFIX_LENGTH = 3
        private const val GIT_COMMAND_FAILED = -1
        private const val GIT_COMMAND_TIMEOUT = -2
        private const val GIT_COMMAND_TIMEOUT_MILLIS = 10_000L
        private const val MAX_PREVIEW_FILE_BYTES = 128_000L

        private val skippedNames =
            setOf(
                ".git",
                ".gradle",
                ".idea",
                ".intellijPlatform",
                ".kotlin",
                "build",
                "intellijPlatform",
                "out",
                "node_modules",
            )
    }
}
