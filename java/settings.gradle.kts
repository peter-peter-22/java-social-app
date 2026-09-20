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
)

project(":users-api").projectDir = file("modules/users/users-api")
project(":users-persistence").projectDir = file("modules/users/users-persistence")

project(":posts-persistence").projectDir = file("modules/posts/posts-persistence")
project(":posts-api").projectDir = file("modules/posts/posts-api")

project(":cockroach-db").projectDir = file("modules/cockroach-db")

project(":object-storage").projectDir = file("modules/uploads_v2/object-storage")