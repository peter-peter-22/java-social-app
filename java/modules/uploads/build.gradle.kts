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
	implementation(project(":modules:object_storage"))
	implementation(project(":modules:users"))
	implementation(project(":modules:cockroach_db"))
	testImplementation(testFixtures(project(":modules:cockroach_db")))
}