plugins {
    id("missionreview.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:entrypoint:api"))
            implementation(project(":feature:review:api"))
            implementation(libs.decompose)
        }
    }
}
