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

    // rabbitmq
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // ffmpeg
    implementation(libs.bramp.ffmpeg)

    // archunit
    testImplementation(libs.archunit.junit5)

    // net.bramp.ffmpeg 0.9.2 pins Guava 33.5.0-jre (see its POM). Keep an eye on its transitive
    // dependencies in the version catalog; do not add a guessed Guava version here.
}

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

                // The integration test inspects the produced files with the same probe the worker uses.
                implementation(libs.bramp.ffmpeg)

                implementation("org.springframework.boot:spring-boot-starter-test")

                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }

            targets {
                all {
                    testTask.configure {
                        description =
                            "Runs the ffmpeg transformation output tests. Requires ffmpeg; run through deployment/test-video-transformations.bash."
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        shouldRunAfter(tasks.test)
                    }
                }
            }
        }
    }
}
