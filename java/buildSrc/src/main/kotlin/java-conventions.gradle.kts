// Basic java conventions, used by all other conventions

plugins {
    java
}

group = "com.example"

repositories {
    mavenCentral()
}

dependencies {
    // lombok
    compileOnly("org.projectlombok:lombok:${Versions.lombok}")
    annotationProcessor("org.projectlombok:lombok:${Versions.lombok}")
    testCompileOnly("org.projectlombok:lombok:${Versions.lombok}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.lombok}")

    // jspecify
    implementation("org.jspecify:jspecify:1.0.0")

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
        // (the Spring Boot BOM in the Spring modules, the Versions pins in the plain Java ones)
        // resolved the agent with it.
        val mockitoCore = classpath.files.firstOrNull { it.name.startsWith("mockito-core-") }
        if (mockitoCore != null)
            jvmArgs("-javaagent:${mockitoCore.absolutePath}")
    }
}