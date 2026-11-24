plugins {
    id("java-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // The architecture guard (clean-media-service.md): the contract cannot import Spring, JPA,
    // Minio, or Jackson.
    testImplementation("com.tngtech.archunit:archunit-junit5:${Versions.archunit}")
}
