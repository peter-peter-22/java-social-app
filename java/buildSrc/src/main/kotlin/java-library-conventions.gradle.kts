// Basic java library without a Spring BOM

plugins {
    `java-library`
    id("java-conventions")
}

// The version catalog; looked up by name like in java-conventions.
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // The test stack of the plain Java modules. No Spring BOM manages these here, so the versions
    // are pinned in the version catalog; the Spring modules resolve the same libraries from the
    // Spring Boot BOM instead (see the Spring conventions).
    testImplementation(catalog.findBundle("java-test").get())
}