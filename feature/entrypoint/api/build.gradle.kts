plugins {
    id("missionreview.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.decompose)
        }
    }
}
