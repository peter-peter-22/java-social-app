pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    "modules:posts",
    "modules:users",
    "modules:cockroach_db",
    // media
    "modules:media:object_storage",
    "modules:media:uploads",
    "modules:media:common",
    "modules:media:image_transformer"
)