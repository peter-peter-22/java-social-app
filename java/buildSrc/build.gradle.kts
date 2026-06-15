// Here we configure a minimal kotlin environment for the conventions.

plugins {
    `kotlin-dsl`
}

dependencies {
    // Here we define the versions of the plugins used in the conventions

    // spring boot
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.7")
    // spring dependency management
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
    // openapi generator
    implementation("org.openapi.generator:org.openapi.generator.gradle.plugin:7.22.0")
    // springdoc
    implementation("org.springdoc.openapi-gradle-plugin:org.springdoc.openapi-gradle-plugin.gradle.plugin:1.9.0")
}

repositories {
    gradlePluginPortal()
}