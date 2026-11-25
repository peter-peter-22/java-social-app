// Basic java conventions, used by all other conventions

plugins {
    java
}

group = "com.example"

repositories {
    mavenCentral()
}

// The version catalog; the entries are looked up by name because Gradle does not generate the
// type-safe libs.* accessors inside precompiled script plugins. The module scripts and
// buildSrc/build.gradle.kts use the typed accessors.
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // lombok
    compileOnly(catalog.findLibrary("lombok").get())
    annotationProcessor(catalog.findLibrary("lombok").get())
    testCompileOnly(catalog.findLibrary("lombok").get())
    testAnnotationProcessor(catalog.findLibrary("lombok").get())

    // jspecify
    implementation(catalog.findLibrary("jspecify").get())

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    doFirst {
        // The mockito agent is the mockito-core jar already on this test task's classpath, so the
        // agent and the mockito core can never be different versions: whatever resolved the core
        // (the Spring Boot BOM in the Spring modules, the version catalog pins in the plain Java
        // ones)
        // resolved the agent with it.
        val mockitoCore = classpath.files.firstOrNull { it.name.startsWith("mockito-core-") }
        if (mockitoCore != null)
            jvmArgs("-javaagent:${mockitoCore.absolutePath}")
    }
}