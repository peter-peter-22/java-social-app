plugins {
	id("web-library-conventions")
	id("test-fixtures-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// modules
	implementation(project(":object-storage"))
	testImplementation(testFixtures(project(":object-storage")))




}