plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

    // jpa
    api("org.springframework.boot:spring-boot-starter-data-jpa")

	// driver
	runtimeOnly("org.postgresql:postgresql")

	// liquibase
	implementation("org.springframework.boot:spring-boot-starter-liquibase")

	// test fixtures
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
	testFixturesImplementation("org.testcontainers:testcontainers-cockroachdb")
	testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
}