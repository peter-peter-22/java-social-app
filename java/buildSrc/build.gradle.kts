// Here we configure a minimal kotlin environment for the conventions.

plugins {
    `kotlin-dsl`
}

dependencies {
    // Here we define the plugins used in the conventions. The versions are pinned in the version
    // catalog (gradle/libs.versions.toml), which buildSrc/settings.gradle.kts imports.

    // spring boot
    implementation(libs.spring.boot.gradle.plugin)
    // spring dependency management
    implementation(libs.spring.dependency.management.gradle.plugin)
    // hibernate orm (bytecode enhancement of the JPA entities in media-registry)
    implementation(libs.hibernate.gradle.plugin)
}

repositories {
    gradlePluginPortal()
}