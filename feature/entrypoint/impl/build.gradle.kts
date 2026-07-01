plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:entrypoint:api"))
            implementation(project(":feature:review:api"))
            implementation(libs.decompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
