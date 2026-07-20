// Basic java conventions, used by all other conventions

plugins {
    java
}

group = "com.example"

repositories {
    mavenCentral()
}

val mockitoAgent by configurations.creating

dependencies {
    // junit
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testCompileOnly("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")

    // jetbrains annotations
    compileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")

    // assertj
    testImplementation("org.assertj:assertj-core:3.27.7")

    // mockito
    testImplementation("org.mockito:mockito-core:5.23.0")
    mockitoAgent("org.mockito:mockito-core:5.23.0") {
        isTransitive = false
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}