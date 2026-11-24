plugins {
    id("java-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // The contract types whose JSON shape this module defines.
    implementation(project(":media-contract"))

    // Jackson 3 databind: the runtime behind the mixins and the shared mapper configuration.
    // The version matches the one the Spring Boot 4 BOM resolves for the deployables;
    // media-json stays Spring-free, so it is pinned here instead of imported from the BOM.
    api("tools.jackson.core:jackson-databind:3.1.4")

    // The architecture guard (clean-media-service.md): media-json cannot import Spring, JPA, or Minio.
    testImplementation("com.tngtech.archunit:archunit-junit5:${Versions.archunit}")
}