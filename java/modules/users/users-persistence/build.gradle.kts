plugins {
    `java-test-fixtures`
    id("web-library-conventions")
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
}