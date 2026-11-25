plugins {
    `java-test-fixtures`
    id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

    // The Minio adapter implements the ObjectStore port of the clean media service.
    // It is an implementation dependency on purpose: the contract types stay behind the port and
    // do not leak onto the compile classpath of the prototype modules using this adapter.
    implementation(project(":media-contract"))

    // minio
    implementation(libs.minio) // PRODUCTION: upgrade

    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // test fixtures
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesImplementation("org.testcontainers:testcontainers-minio")
}