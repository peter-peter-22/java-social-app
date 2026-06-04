plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// liquibase
	implementation("org.springframework.boot:spring-boot-starter-liquibase")

	// test fixtures
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testFixturesImplementation("org.testcontainers:testcontainers-cockroachdb")
	testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
}