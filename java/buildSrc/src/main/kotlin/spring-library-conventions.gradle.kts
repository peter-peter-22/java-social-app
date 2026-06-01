// Spring boot library, no main function

// The build function of the spring boot plugin is disabled because there is no main function in a library.
// As a side effect, 2 additional configurations (mavenBom, JavaCompile) must be added manually.

plugins {
    id("java-library-conventions")
    id("java-conventions")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Enable the automatic dependency version management of spring
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}