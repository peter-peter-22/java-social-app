plugins {
	id("web-project-conventions")
}

version = "0.0.1-SNAPSHOT"

val integrationTest by sourceSets.creating

integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
	// lipvips
	implementation("app.photofox.vips-ffm:vips-ffm-core:1.9.8")

	// modules
	implementation(project(":media-api"))
	implementation(project(":object-storage"))
}

tasks.register<Test>("integrationTest") {
	description = "Runs integration tests that require libvips."
	group = LifecycleBasePlugin.VERIFICATION_GROUP

	testClassesDirs = integrationTest.output.classesDirs
	classpath = integrationTest.runtimeClasspath
	shouldRunAfter(tasks.test)
	useJUnitPlatform()
}
