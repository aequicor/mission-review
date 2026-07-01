plugins {
    id("missionreview.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:storage"))
        }
    }
}
