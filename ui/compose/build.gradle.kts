plugins {
    id("missionreview.kotlin-jvm")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(project(":core:navigation"))
    implementation(project(":feature:entrypoint:api"))
    implementation(project(":feature:review:api"))
}
