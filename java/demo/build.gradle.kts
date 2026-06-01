plugins {
	id("web-project-conventions")
}

version = "0.0.1-SNAPSHOT"

dependencies {
}

openApi {
	outputDir.set(file("$rootDir/docs"))
	outputFileName.set("swagger.json")
}