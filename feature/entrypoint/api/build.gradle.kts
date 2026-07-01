plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.decompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
