// Spring boot project that can generate openapi documentation
// default swagger url: http://localhost:8080/swagger-ui.html

plugins {
    id("web-library-conventions")
    id("org.openapi.generator")
    // creates the "./gradlew generateOpenApiDocs" task
    id("org.springdoc.openapi-gradle-plugin")
}

dependencies {
}