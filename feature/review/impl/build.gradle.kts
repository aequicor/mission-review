plugins {
    id("missionreview.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:review:api"))
            implementation(project(":core:code-render"))
            implementation(project(":core:git"))
            implementation(project(":core:mcp"))
            implementation(project(":core:storage"))
            implementation(libs.decompose)
        }
    }
}
