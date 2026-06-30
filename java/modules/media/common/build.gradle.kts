plugins {
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// modules
	implementation(project(":modules:users"))
}