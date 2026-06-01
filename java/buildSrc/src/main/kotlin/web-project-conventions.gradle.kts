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
    implementation("org.springframework.boot:spring-boot-starter-web")
    // openapi UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
}