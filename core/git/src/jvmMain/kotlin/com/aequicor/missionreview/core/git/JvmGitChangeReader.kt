package com.aequicor.missionreview.core.git

import java.io.File

class JvmGitChangeReader(
    private val processRunner: GitProcessRunner = GitProcessRunner(),
) : GitChangeReader {

    override fun readChanges(repository: GitRepository): List<GitChangedFile> {
        val stagedFiles = processRunner
            .run(repository.rootPath, "diff", "--name-only", "--cached")
            .toFileList()
            .map { path ->
                GitChangedFile(
                    path = path,
                    status = GitChangeStatus.STAGED,
                    diff = processRunner.run(repository.rootPath, "diff", "--cached", "--", path),
                )
            }

        val untrackedFiles = processRunner
            .run(repository.rootPath, "ls-files", "--others", "--exclude-standard")
            .toFileList()
            .map { path ->
                GitChangedFile(
                    path = path,
                    status = GitChangeStatus.UNTRACKED,
                    content = File(repository.rootPath).resolve(path).takeIf { it.isFile }?.readText(),
                )
            }

        return stagedFiles + untrackedFiles
    }

    private fun String.toFileList(): List<String> =
        lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toList()
}

class GitProcessRunner {
    fun run(repositoryRoot: String, vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git") + arguments)
            .directory(File(repositoryRoot))
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        check(exitCode == 0) {
            "git ${arguments.joinToString(" ")} failed with exit code $exitCode: $output"
        }

        return output
    }
}
