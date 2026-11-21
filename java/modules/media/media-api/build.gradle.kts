plugins {
    id("spring-project-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // modules
    implementation(project(":media-contract"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // The application services own their transaction boundaries. The transaction manager itself
    // arrives with the registry adapters when the API is wired.
    implementation("org.springframework:spring-tx")
}
