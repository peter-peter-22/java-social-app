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
    // The Minio adapter behind the ObjectStore port. The worker moves files through object
    // storage and nothing else: no registry, no database, no upload services.
    implementation(project(":object-storage"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // The async image transform entry point: the worker consumes commands from RabbitMQ.
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // libvips
    implementation(libs.vips.ffm.core)

    // The architecture guard (clean-media-service.md): the worker cannot import the registry.
    testImplementation(libs.archunit.junit5)
}

// libvips loads its native library through the foreign function and memory API. Java 25 blocks
// native access without this flag, which breaks vips-ffm. Only this deployable loads libvips, so
// the flag lives here; the container run command carries it for the packaged jar.
tasks.withType<Test>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("bootRun") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation(project(":media-contract"))

                implementation("org.springframework.boot:spring-boot-starter-test")

                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs the libvips transformation output tests. Requires libvips; run through deployment/test-transformations.bash."
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        shouldRunAfter(tasks.test)
                    }
                }
            }
        }
    }
}
