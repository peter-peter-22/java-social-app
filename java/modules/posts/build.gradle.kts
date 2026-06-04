plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	runtimeOnly("org.postgresql:postgresql")

	// test container usage
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")

	// modules
	implementation(project(":modules:users"))
	testImplementation(testFixtures(project(":modules:cockroach_db")))
}