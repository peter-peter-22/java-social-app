pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    "modules:application",
    "modules:demo",
    "modules:posts",
    "modules:users",
    "modules:cockroach_db",
    "modules:minio",
    "modules:uploads",
    "modules:utils"
)