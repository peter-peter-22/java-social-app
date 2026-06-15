// Spring boot library that can define openapi compatible controllers

plugins {
    id("spring-library-conventions")
}

dependencies {
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    // openapi annotations and others
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
}