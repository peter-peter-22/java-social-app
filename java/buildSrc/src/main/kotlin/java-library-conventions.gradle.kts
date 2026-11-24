// Basic java library without a Spring BOM

plugins {
    `java-library`
    id("java-conventions")
}

dependencies {
    // The test stack of the plain Java modules. No Spring BOM manages these here, so the versions
    // are pinned from the shared Versions; the Spring modules resolve the same libraries from the
    // Spring Boot BOM instead (see the Spring conventions).
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("org.assertj:assertj-core:${Versions.assertj}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
}