plugins {
	`java-test-fixtures`
	id("spring-library-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {

	// minio
	implementation("io.minio:minio:8.6.0")

	// validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// rest client for testing
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	// test fixtures
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
	testFixturesImplementation("org.testcontainers:testcontainers-minio")
}