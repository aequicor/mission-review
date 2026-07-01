plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core:di"))
    implementation(project(":core:git"))
    implementation(project(":core:mcp"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:storage"))
    implementation(project(":feature:entrypoint:impl"))
    implementation(project(":feature:review:impl"))
    implementation(project(":ui:compose"))
    implementation(libs.decompose)
}

compose.desktop {
    application {
        mainClass = "com.aequicor.missionreview.desktop.MainKt"
    }
}
