plugins {
	id("web-project-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
	// lipvips
	implementation("app.photofox.vips-ffm:vips-ffm-core:1.9.8")

	// modules
	implementation(project(":modules:media:common"))
}