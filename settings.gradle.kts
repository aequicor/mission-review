import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.10.2"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

rootProject.name = "mission-review"

include(
    ":app:desktop",
    ":app:intellij-plugin",
    ":core:code-render",
    ":core:di",
    ":core:git",
    ":core:mcp",
    ":core:navigation",
    ":core:network",
    ":core:storage",
    ":core:theme",
    ":core:ui-contracts",
    ":ui:compose",
    ":ui:intellij",
    ":feature:entrypoint:api",
    ":feature:entrypoint:impl",
    ":feature:review:api",
    ":feature:review:impl",
)
