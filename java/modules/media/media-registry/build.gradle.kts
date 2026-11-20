plugins {
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
    implementation(project(":media-contract"))
    testImplementation(testFixtures(project(":cockroach-db")))

    // Hibernate 7.2 persists the jsonb transformation specs with the Jackson 2 databind.
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
