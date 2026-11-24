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
    // hibernate orm (bytecode enhancement of the JPA entities in media-registry)
    implementation("org.hibernate.orm:hibernate-gradle-plugin:7.2.19.Final")
}

repositories {
    gradlePluginPortal()
}