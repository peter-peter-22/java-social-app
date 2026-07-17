plugins {
	id("web-library-conventions")
	id("test-fixtures-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// modules
	implementation(project(":object-storage"))
	testImplementation(testFixtures(project(":object-storage")))

	implementation(project(":uploads-persistence"))
	testImplementation(testFixtures(project(":uploads-persistence")))

	implementation(project(":users-api"))

	implementation(project(":uploads-api"))
	testImplementation(testFixtures(project(":uploads-api")))

	// test fixtures
	testFixturesImplementation(testFixtures(project(":users-persistence")))
}