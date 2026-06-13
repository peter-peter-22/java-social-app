plugins {
	id("web-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// test container usage
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")

	// minio
	implementation("io.minio:minio:8.6.0")

}