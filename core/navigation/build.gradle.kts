plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(project(":feature:entrypoint:api"))
            api(project(":feature:review:api"))
            api(libs.decompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
