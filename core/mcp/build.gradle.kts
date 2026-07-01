plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:storage"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
