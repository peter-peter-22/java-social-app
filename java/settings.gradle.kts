pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    ":cockroach-db",
    // posts
    ":posts-persistence",
    ":posts-api",
    // users
    ":users-api",
    ":users-persistence",
    // uploads
    ":object-storage",
    ":uploads-service",
    ":uploads-api",
    ":image-transformer",
    ":uploads-persistence"
)

project(":users-api").projectDir = file("modules/users/users-api")
project(":users-persistence").projectDir = file("modules/users/users-persistence")

project(":object-storage").projectDir = file("modules/uploads/object-storage")
project(":uploads-service").projectDir = file("modules/uploads/uploads-service")
project(":uploads-api").projectDir = file("modules/uploads/uploads-api")
project(":uploads-persistence").projectDir = file("modules/uploads/uploads-persistence")
project(":image-transformer").projectDir = file("modules/uploads/image-transformer")

project(":posts-persistence").projectDir = file("modules/posts/posts-persistence")
project(":posts-api").projectDir = file("modules/posts/posts-api")

project(":cockroach-db").projectDir = file("modules/cockroach-db")