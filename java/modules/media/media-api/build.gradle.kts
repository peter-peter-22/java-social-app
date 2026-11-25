plugins {
    id("spring-project-conventions")
    `jvm-test-suite`
    idea
}

version = "0.0.1-SNAPSHOT"

idea {
    module {
        testSources.from(file("src/integrationTest/java"))
        testResources.from(file("src/integrationTest/resources"))
    }
}

// The docker images the compose stack runs: the JDK + libvips development image of the worker and
// the nginx edge that verifies the download tokens. Building them is cached by docker and cheap
// when the images exist; one command then covers the whole end-to-end run:
// ./gradlew :media-api:integrationTest
val buildComposeImages by tasks.registering(Exec::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Builds the docker images used by the compose stack: the worker's dev image and the nginx edge."
    workingDir = file("../deployment")
    commandLine("bash", "build.bash")
}

dependencies {
    // modules
    implementation(project(":media-contract"))
    // The shared JSON shape of the worker messages and the transformation specs.
    implementation(project(":media-json"))
    // The driven adapters: the JPA registry and the Minio object store behind the contract ports.
    implementation(project(":media-registry"))
    implementation(project(":object-storage"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // The async image transform port: the API publishes commands to RabbitMQ and consumes the
    // dead-lettered attempts to mark them failed.
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // The application services own their transaction boundaries. The transaction manager itself
    // arrives with the registry adapters when the API is wired.
    implementation("org.springframework:spring-tx")

    // The API runs the registry's migrations and owns the persistence context; the worker has no
    // database. The driver and the changelog arrive with the registry adapter.
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    // The context smoke test loads the real adapters against a testcontainer database.
    testImplementation(testFixtures(project(":cockroach-db")))
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation(project(":media-contract"))
                // The Java client drives the end-to-end test: HTTP only, no in-process ports.
                implementation(project(":media-client"))

                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-webmvc")
                implementation("org.springframework.boot:spring-boot-starter-webmvc-test")

                // The compose stack the end-to-end test boots (ComposeContainer, local compose mode).
                implementation("org.testcontainers:testcontainers")

                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs the end-to-end test: boots the compose stack and drives the API through the media client."
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        shouldRunAfter(tasks.test)
                        // The compose stack runs the worker jar in the dev image and verifies the
                        // download tokens in the edge image; all must exist before the stack starts.
                        dependsOn(":image-worker:bootJar", buildComposeImages)
                    }
                }
            }
        }
    }
}
