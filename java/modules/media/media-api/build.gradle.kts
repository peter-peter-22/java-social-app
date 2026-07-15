plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
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