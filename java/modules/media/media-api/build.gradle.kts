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

                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-webmvc")
                implementation("org.springframework.boot:spring-boot-starter-webmvc-test")

                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs the end-to-end test that requires the image worker, the database, and the object storage."
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        shouldRunAfter(tasks.test)
                    }
                }
            }
        }
    }
}
