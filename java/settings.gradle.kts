pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    ":posts",
    ":cockroach-db",
    // users
    ":users-api",
    ":users-persistence",
    // uploads
    ":object-storage",
    ":uploads-service",
    ":uploads-api",
    ":image-transformer",
    ":uploads-persistence",
    // utils
    ":spring-utils"
)

project(":users-api").projectDir = file("modules/users/users-api")
project(":users-persistence").projectDir = file("modules/users/users-persistence")
project(":object-storage").projectDir = file("modules/uploads/object-storage")
project(":uploads-service").projectDir = file("modules/uploads/uploads-service")
project(":uploads-api").projectDir = file("modules/uploads/uploads-api")
project(":uploads-persistence").projectDir = file("modules/uploads/uploads-persistence")
project(":image-transformer").projectDir = file("modules/uploads/image-transformer")
project(":posts").projectDir = file("modules/posts")
project(":cockroach-db").projectDir = file("modules/cockroach-db")
project(":spring-utils").projectDir = file("modules/utils/spring-utils")