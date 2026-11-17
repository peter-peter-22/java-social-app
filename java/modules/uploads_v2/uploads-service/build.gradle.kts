plugins {
	id("web-library-conventions")
	id("test-fixtures-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jpa
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// modules
	implementation(project(":object-storage"))
	testImplementation(testFixtures(project(":object-storage")))

    implementation(project(":upload-api"))
	implementation(project(":upload-services"))

	implementation(project(":upload-registry"))
}