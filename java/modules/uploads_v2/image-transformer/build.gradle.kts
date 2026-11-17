@file:Suppress("UnstableApiUsage")

plugins {
    idea
    `java-test-fixtures`
    `jvm-test-suite`
    id("web-project-conventions")
}

version = "0.0.1-SNAPSHOT"

idea {
    module {
        testSources.from(file("src/integrationTest/java"))
        testResources.from(file("src/integrationTest/resources"))
    }
}

dependencies {
    // lipvips
    implementation("app.photofox.vips-ffm:vips-ffm-core:1.9.8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // modules
    implementation(project(":upload-api"))
    implementation(project(":upload-services"))
    implementation(project(":object-storage"))
    implementation(project(":upload-registry"))

    // test fixtures
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation(testFixtures(project()))

                implementation(project(":upload-api"))
                implementation(project(":upload-services"))
                implementation(project(":object-storage"))
                implementation(project(":upload-registry"))

                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-webmvc")

                implementation("com.squareup.okhttp3:mockwebserver3:5.0.0-alpha.12")

                runtimeOnly("org.junit.platform:junit-platform-launcher")
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs integration tests requiring libvips."
                        group = LifecycleBasePlugin.VERIFICATION_GROUP
                        shouldRunAfter(tasks.test)
                        jvmArgs("--enable-native-access=ALL-UNNAMED") // Fix the warning about vips-ffm native access
                    }
                }
            }
        }
    }
}

sourceSets.named("integrationTest") {
    resources.srcDir("src/test/resources")
}