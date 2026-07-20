plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// modules
	implementation(project(":posts-api"))
	implementation(project(":cockroach-db"))
	testImplementation(testFixtures(project(":cockroach-db")))
}