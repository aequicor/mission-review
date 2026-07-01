plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:di"))
    implementation(project(":core:git"))
    implementation(project(":core:mcp"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:storage"))
    implementation(project(":feature:entrypoint:impl"))
    implementation(project(":feature:review:impl"))
    implementation(project(":ui:intellij"))
    implementation(libs.decompose)
}
