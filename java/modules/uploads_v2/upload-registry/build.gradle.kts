plugins {
    `java-test-fixtures`
    id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // modules
    implementation(project(":cockroach-db"))
    testImplementation(testFixtures(project(":cockroach-db")))
    implementation(project(":upload-api"))
}