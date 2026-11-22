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
    // The shared JSON shape of the jsonb transformation specs.
    implementation(project(":media-json"))
    testImplementation(testFixtures(project(":cockroach-db")))

    // The properties customizer that installs the shared JSON format mapper into Hibernate.
    implementation("org.springframework.boot:spring-boot-hibernate")
}
