import gradle.kotlin.dsl.accessors._a252323f2477ad123f0aa5aba7a9238a.implementation
import gradle.kotlin.dsl.accessors._a252323f2477ad123f0aa5aba7a9238a.testImplementation

// Spring boot project that can generate openapi documentation
// default swagger url: http://localhost:8080/swagger-ui.html

plugins {
    id("spring-project-conventions")
    id("org.openapi.generator")
    // creates the "./gradlew generateOpenApiDocs" task
    id("org.springdoc.openapi-gradle-plugin")
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