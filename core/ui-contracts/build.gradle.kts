plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:code-render"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
