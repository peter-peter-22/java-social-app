plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// modules
	implementation(project(":users-api"))
}

configurations.named("testFixturesImplementation") {
	extendsFrom(configurations.implementation.get())
}

configurations.named("testFixturesRuntimeOnly") {
	extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
}

configurations.named("testFixturesAnnotationProcessor") {
	extendsFrom(configurations.annotationProcessor.get())
}

configurations.named("testFixturesCompileOnly") {
	extendsFrom(configurations.compileOnly.get())
}
