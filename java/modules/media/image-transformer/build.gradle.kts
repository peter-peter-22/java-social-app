plugins {
	idea
	id("web-project-conventions")
}

version = "0.0.1-SNAPSHOT"

idea {
	module {
		testSources.from(file("src/integrationTest/java"))
		testResources.from(file("src/integrationTest/resources"))
	}
}

dependencies {
	// lipvips
	implementation("app.photofox.vips-ffm:vips-ffm-core:1.9.8")

	// modules
	implementation(project(":media-api"))
	implementation(project(":object-storage"))
}

testing {
	suites {
		register<JvmTestSuite>("integrationTest") {
			dependencies {
				implementation(project())
				implementation(project(":media-api"))
				implementation("org.springframework.boot:spring-boot-starter-test")
			}

			targets {
				all {
					testTask.configure {
						description = "Runs integration tests that require libvips."
						group = LifecycleBasePlugin.VERIFICATION_GROUP
						shouldRunAfter(tasks.test)
					}
				}
			}
		}
	}
}
