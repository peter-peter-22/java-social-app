pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    "modules:posts",
    "modules:cockroach_db",
    // users
    "modules:users:api",
    "modules:users:persistence",
    // media
    "modules:media:object_storage",
    "modules:media:uploads",
    "modules:media:common",
    "modules:media:image_transformer"
)