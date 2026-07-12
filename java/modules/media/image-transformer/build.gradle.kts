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
}

testing {
	suites {
		register<JvmTestSuite>("integrationTest") {
			dependencies {
				implementation(project())
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

// copy main deps to integration test

configurations.named("integrationTestImplementation") {
	extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
}

configurations.named("integrationTestRuntimeOnly") {
	extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
}

configurations.named("integrationTestAnnotationProcessor") {
	extendsFrom(configurations.annotationProcessor.get())
}

configurations.named("integrationTestCompileOnly") {
	extendsFrom(configurations.compileOnly.get())
}

// copy main deps to test fixture

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
