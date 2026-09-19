plugins {
    `java-test-fixtures`
    id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // modules
    api(project(":uploads-api"))
    testImplementation(testFixtures(project(":uploads-api")))
    implementation(project(":object-storage"))

    // test fixtures
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation(testFixtures(project(":uploads-api")))
}