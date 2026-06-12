plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// test container usage
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")

	// modules
	implementation(project(":modules:minio"))
	implementation(project(":modules:users"))
	testImplementation(testFixtures(project(":modules:cockroach_db")))
}