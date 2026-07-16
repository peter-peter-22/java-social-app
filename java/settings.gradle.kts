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
    // media
    ":object-storage",
    ":uploads",
    ":uploads-api",
    ":image-transformer",
    // utils
    ":spring-utils"
)

project(":users-api").projectDir = file("modules/users/users-api")
project(":users-persistence").projectDir = file("modules/users/users-persistence")
project(":object-storage").projectDir = file("modules/media/object-storage")
project(":uploads").projectDir = file("modules/media/uploads")
project(":uploads-api").projectDir = file("modules/media/uploads-api")
project(":image-transformer").projectDir = file("modules/media/image-transformer")
project(":posts").projectDir = file("modules/posts")
project(":cockroach-db").projectDir = file("modules/cockroach-db")
project(":spring-utils").projectDir = file("modules/utils/spring-utils")