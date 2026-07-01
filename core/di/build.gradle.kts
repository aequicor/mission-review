plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:git"))
            implementation(project(":core:mcp"))
            implementation(project(":core:navigation"))
            implementation(project(":core:network"))
            implementation(project(":core:storage"))
            implementation(project(":feature:entrypoint:impl"))
            implementation(project(":feature:review:impl"))
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
