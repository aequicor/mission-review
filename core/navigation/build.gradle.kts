plugins {
    id("missionreview.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature:entrypoint:api"))
            api(project(":feature:review:api"))
            api(libs.decompose)
        }
    }
}
