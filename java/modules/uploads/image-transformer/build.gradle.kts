@file:Suppress("UnstableApiUsage")

plugins {
	idea
	`java-test-fixtures`
	`jvm-test-suite`
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
	implementation(project(":uploads-api"))
	implementation(project(":object-storage"))

	// testing
	testImplementation(testFixtures(project(":uploads-api")))

	// test fixtures
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesImplementation(project(":uploads-api"))
}

testing {
	suites {
		register<JvmTestSuite>("integrationTest") {
			dependencies {
				implementation(project())
				implementation(testFixtures(project()))

				implementation("org.springframework.boot:spring-boot-starter-test")
				runtimeOnly("org.junit.platform:junit-platform-launcher")
				implementation(project(":uploads-api"))
			}

			targets {
				all {
					testTask.configure {
						description = "Runs integration tests requiring libvips."
						group = LifecycleBasePlugin.VERIFICATION_GROUP
						shouldRunAfter(tasks.test)
					}
				}
			}
		}
	}
}

// CLEAN: should this be grouped with other docker test tasks in the future?
// CLEAN: should I use the docker test plugin?
tasks.register<Exec>("dockerIT") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Runs image-transformer integration tests inside Docker."

	workingDir(rootProject.projectDir)

	commandLine(
		"docker", "compose",
		"-f", "modules/uploads/image-transformer/docker/compose-test.yaml",
		"run", "--rm",
		"image-transformer"
	)
}