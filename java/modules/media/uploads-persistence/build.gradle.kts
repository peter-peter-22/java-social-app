plugins {
	id("web-library-conventions")
	id("test-fixtures-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// modules
	implementation(project(":users-api"))

	implementation(project(":cockroach-db"))
	testImplementation(testFixtures(project(":cockroach-db")))

	implementation(project(":uploads-api"))
	testImplementation(testFixtures(project(":uploads-api")))

	testImplementation(testFixtures(project(":users-persistence")))

	// test fixtures
	testFixturesImplementation(testFixtures(project(":users-persistence")))
}