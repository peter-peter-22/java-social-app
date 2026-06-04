import gradle.kotlin.dsl.accessors._90f717c323b2b960ec4f135944368af3.implementation

// Spring boot library that can define openapi compatible controllers

plugins {
    id("spring-library-conventions")
}

dependencies {
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    // openapi annotations and others
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
}