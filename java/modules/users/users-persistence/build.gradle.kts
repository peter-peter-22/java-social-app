plugins {
    id("web-library-conventions")
    id("test-fixtures-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

    // jdbc
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    // test containers
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")

    // modules
    implementation(project(":users-api"))
    implementation(project(":cockroach-db"))
    testImplementation(testFixtures(project(":cockroach-db")))

    // test fixtures
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}