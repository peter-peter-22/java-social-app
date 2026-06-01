// Here we configure a minimal kotlin environment for the conventions.

plugins {
    `kotlin-dsl`
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.17.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

dependencies {
    // Here we define the versions of the plugins used in the conventions
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.6")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
    implementation("org.openapi.generator:org.openapi.generator.gradle.plugin:7.22.0")
    implementation("org.springdoc.openapi-gradle-plugin:org.springdoc.openapi-gradle-plugin.gradle.plugin:1.9.0")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}