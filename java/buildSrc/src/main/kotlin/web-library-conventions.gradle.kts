// Spring boot library that can define openapi compatible controllers

plugins {
    id("spring-library-conventions")
}

dependencies {
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
}