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
	implementation(project(":object-storage"))
	testImplementation(testFixtures(project(":object-storage")))

	implementation(project(":users-api"))

	testImplementation(testFixtures(project(":users-persistence")))

	implementation(project(":cockroach-db"))
	testImplementation(testFixtures(project(":cockroach-db")))

	implementation(project(":uploads-api"))
	testImplementation(testFixtures(project(":uploads-api")))

	// test fixtures
	testFixturesImplementation(testFixtures(project(":users-persistence")))
}


configurations {
	testFixturesImplementation {
		extendsFrom(implementation.get())
	}

	testFixturesRuntimeOnly {
		extendsFrom(testRuntimeOnly.get())
	}

	testFixturesCompileOnly {
		extendsFrom(compileOnly.get())
	}

	testFixturesAnnotationProcessor {
		extendsFrom(annotationProcessor.get())
	}
}