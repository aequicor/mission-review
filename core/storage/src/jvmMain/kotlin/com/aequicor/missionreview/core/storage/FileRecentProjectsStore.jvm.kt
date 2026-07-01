package com.aequicor.missionreview.core.storage

import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * File-backed recent projects store for JVM desktop targets.
 *
 * The file format is private to this class and may change between versions.
 */
class FileRecentProjectsStore(
    private val storageFile: Path = defaultRecentProjectsFile(),
    private val maxProjects: Int = DEFAULT_MAX_PROJECTS,
    private val clock: () -> Long = System::currentTimeMillis,
) : RecentProjectsStore {

    override fun loadRecentProjects(): List<RecentProject> =
        runCatching { readProjects() }.getOrDefault(emptyList())

    override fun rememberProject(path: String): List<RecentProject> {
        val normalizedPath = normalizePath(path)
        val currentProjects = loadRecentProjects()
        val updatedProjects =
            listOf(
                RecentProject(
                    path = normalizedPath,
                    name = displayNameFor(normalizedPath),
                    lastOpenedAtEpochMillis = clock(),
                ),
            ) + currentProjects.filterNot { it.path == normalizedPath }

        val limitedProjects = updatedProjects.take(maxProjects)
        writeProjects(limitedProjects)

        return limitedProjects
    }

    private fun readProjects(): List<RecentProject> {
        if (!Files.isRegularFile(storageFile)) {
            return emptyList()
        }

        return Files.readAllLines(storageFile, StandardCharsets.UTF_8)
            .mapNotNull(::parseProjectLine)
            .distinctBy(RecentProject::path)
            .sortedByDescending(RecentProject::lastOpenedAtEpochMillis)
            .take(maxProjects)
    }

    private fun parseProjectLine(line: String): RecentProject? {
        val timestamp = line.substringBefore('\t').toLongOrNull() ?: return null
        val encodedPath = line.substringAfter('\t', missingDelimiterValue = "")
        if (encodedPath.isBlank()) {
            return null
        }

        val path = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8)
        return RecentProject(
            path = path,
            name = displayNameFor(path),
            lastOpenedAtEpochMillis = timestamp,
        )
    }

    private fun writeProjects(projects: List<RecentProject>) {
        Files.createDirectories(storageFile.parent)

        val lines =
            projects.map { project ->
                val encodedPath = URLEncoder.encode(project.path, StandardCharsets.UTF_8)
                "${project.lastOpenedAtEpochMillis}\t$encodedPath"
            }

        val temporaryFile = storageFile.resolveSibling("${storageFile.fileName}.tmp")
        Files.write(temporaryFile, lines, StandardCharsets.UTF_8)
        try {
            Files.move(
                temporaryFile,
                storageFile,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: IOException) {
            Files.move(
                temporaryFile,
                storageFile,
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    private fun normalizePath(path: String): String =
        Paths.get(path).toAbsolutePath().normalize().toString()

    private fun displayNameFor(path: String): String {
        val normalizedPath = Paths.get(path).toAbsolutePath().normalize()
        return normalizedPath.fileName?.toString() ?: normalizedPath.toString()
    }

    private companion object {
        private const val DEFAULT_MAX_PROJECTS = 12
    }
}

/**
 * Returns the default user-local file for recent projects.
 */
fun defaultRecentProjectsFile(): Path {
    val osName = System.getProperty("os.name").lowercase()
    val userHome = Paths.get(System.getProperty("user.home"))

    return when {
        osName.contains("win") -> {
            val appData = System.getenv("APPDATA")?.takeIf(String::isNotBlank)
            Paths.get(appData ?: userHome.resolve("AppData/Roaming").toString())
                .resolve("mission-review/recent-projects.txt")
        }

        osName.contains("mac") ->
            userHome.resolve("Library/Application Support/mission-review/recent-projects.txt")

        else ->
            userHome.resolve(".local/share/mission-review/recent-projects.txt")
    }
}
