plugins {
	`java-test-fixtures`
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// modules
	implementation(project(":modules:media:object_storage"))
	implementation(project(":modules:users:api"))
	implementation(project(":modules:users:persistence"))
	implementation(project(":modules:cockroach_db"))
	testImplementation(testFixtures(project(":modules:cockroach_db")))
	implementation(project(":modules:media:media_api"))
}
