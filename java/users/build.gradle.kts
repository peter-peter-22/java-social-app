plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	runtimeOnly("org.postgresql:postgresql")

	// liquibase
	implementation("org.springframework.boot:spring-boot-starter-liquibase")

	// test containers
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-cockroachdb")
	testImplementation("org.testcontainers:testcontainers-postgresql")
}