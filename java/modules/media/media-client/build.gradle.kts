plugins {
    id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
    // modules: the contract types and the shared media JSON shape the wire format speaks.
    api(project(":media-contract"))
    api(project(":media-json"))

    // The outbound HTTP client: the shared Spring RestClient (root AGENTS.md conventions). The
    // builder type is part of this module's API, so consumers see it.
    api("org.springframework:spring-web")

    // The architecture guard: the client talks HTTP only and never touches the server internals.
    testImplementation(libs.archunit.junit5)
}
