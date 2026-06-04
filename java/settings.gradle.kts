pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    "application",
    "demo",
    "posts",
    "users",
    "cockroach_db"
)