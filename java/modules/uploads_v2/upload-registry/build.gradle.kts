plugins {
    `java-test-fixtures`
    id("spring-library-conventions")
    id("org.hibernate.orm") version "7.2.19.Final"
}

hibernate {
    enhancement {}
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // modules
    implementation(project(":cockroach-db"))
    testImplementation(testFixtures(project(":cockroach-db")))
    implementation(project(":upload-api"))
    implementation(project(":object-storage"))
}
