// Spring boot library

plugins {
    `java-library`
    id("java-conventions")
    // The Spring Boot plugin is NOT applied to libraries: there is no main function, so the boot
    // tasks (bootJar, bootRun) must not exist here. The plugin is only on the buildSrc classpath,
    // for the BOM coordinates referenced below. As a side effect, 2 additional configurations
    // (mavenBom, JavaCompile) must be added manually.
    //
    // Note: never request the plugin here with `apply false` — Gradle still applies the plugin
    // from a precompiled script's plugins block in that case, which re-adds the boot tasks and
    // breaks `build` on libraries (bootJar fails on the missing main class).
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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