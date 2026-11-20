plugins {
    id("java-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // Jackson 2 annotations define the persisted (jsonb) and wire JSON shape of the polymorphic
    // transformation specs and worker messages. Jackson 3 databind reads the same annotation types.
    // The version matches the one the Spring Boot 4 BOM resolves for the prototype modules;
    // media-contract stays Spring-free, so it is pinned here instead of imported from the BOM.
    api("com.fasterxml.jackson.core:jackson-annotations:2.21")

    // Round-trip tests for the JSON shapes, matching the runtime serializer of the deployables.
    testImplementation("tools.jackson.core:jackson-databind:3.1.4")
}
