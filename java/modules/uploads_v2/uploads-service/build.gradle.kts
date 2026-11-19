plugins {
	id("web-project-conventions")
	id("test-fixtures-conventions")
	`jvm-test-suite`
	idea
}

version = "0.0.1-SNAPSHOT"

idea {
	module {
		testSources.from(file("src/integrationTest/java"))
		testResources.from(file("src/integrationTest/resources"))
	}
}

dependencies {
	// jpa
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// modules
	implementation(project(":object-storage"))
	testImplementation(testFixtures(project(":object-storage")))

    implementation(project(":upload-api"))
	implementation(project(":upload-services"))

	implementation(project(":upload-registry"))
}

testing {
	suites {
		register<JvmTestSuite>("integrationTest") {
			dependencies {
				implementation(project())

				implementation(project(":upload-api"))
				implementation(project(":upload-services"))
				implementation(project(":object-storage"))
				implementation(project(":upload-registry"))

				implementation("org.springframework.boot:spring-boot-starter-test")
				implementation("org.springframework.boot:spring-boot-starter-webmvc")
				implementation("org.springframework.boot:spring-boot-starter-webmvc-test")

				implementation("com.squareup.okhttp3:mockwebserver3:5.0.0-alpha.12")

				runtimeOnly("org.junit.platform:junit-platform-launcher")
			}

			targets {
				all {
					testTask.configure {
						description = "Runs the end-to-end integration test that requires the image worker, the database, and the object storage."
						group = LifecycleBasePlugin.VERIFICATION_GROUP
						shouldRunAfter(tasks.test)
					}
				}
			}
		}
	}
}