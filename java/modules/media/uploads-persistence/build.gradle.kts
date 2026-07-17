plugins {
	`java-test-fixtures`
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jdbc
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// modules
	implementation(project(":users-api"))

	testImplementation(testFixtures(project(":users-persistence")))

	implementation(project(":cockroach-db"))
	testImplementation(testFixtures(project(":cockroach-db")))

	implementation(project(":uploads-api"))
	testImplementation(testFixtures(project(":uploads-api")))

	// test fixtures
	testFixturesImplementation(testFixtures(project(":users-persistence")))
	testFixturesImplementation(project(":uploads-api"))
	testFixturesImplementation(project(":users-api"))

	testFixturesCompileOnly("org.projectlombok:lombok:1.18.46")
	testFixturesAnnotationProcessor("org.projectlombok:lombok:1.18.46")

	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")

}