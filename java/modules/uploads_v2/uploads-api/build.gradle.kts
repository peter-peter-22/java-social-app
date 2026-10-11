plugins {
	id("java-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// jackson
	implementation("com.fasterxml.jackson.core:jackson-core:2.21.5")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.21")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.21.5")
}