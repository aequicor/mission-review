pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
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
