plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// modules
	implementation(project(":object-storage"))
	implementation(project(":users-api"))
	implementation(project(":users-persistence"))
	implementation(project(":cockroach-db"))
	testImplementation(testFixtures(project(":cockroach-db")))
	implementation(project(":media-api"))
}
