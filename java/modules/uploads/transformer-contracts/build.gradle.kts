plugins {
    `java-test-fixtures`
    id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":uploads-api"))
    implementation(project(":object-storage"))

    // test fixtures
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation(project(":uploads-api"))
}