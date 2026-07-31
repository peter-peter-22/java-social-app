plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// jackson
	implementation("com.fasterxml.jackson.core:jackson-core:2.21.5")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.21")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.21.5")

	// modules
	implementation(project(":users-api"))
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