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
	implementation(project(":media-api"))
	implementation(project(":object-storage"))

	// testing
	testImplementation(testFixtures(project(":media-api")))
}

testing {
	suites {
		register<JvmTestSuite>("integrationTest") {
			dependencies {
				implementation(project())
				implementation(testFixtures(project()))
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

// copy deps to integration test

configurations{
	named("integrationTestImplementation") {
		extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
	}

	named("integrationTestRuntimeOnly") {
		extendsFrom(configurations.testRuntimeOnly.get())
	}

	named("integrationTestAnnotationProcessor") {
		extendsFrom(configurations.annotationProcessor.get())
	}
	named("integrationTestCompileOnly") {
		extendsFrom(configurations.compileOnly.get())
	}
}

// copy deps to test fixture

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