pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    ":cockroach-db",
    // uploads
    ":object-storage",
    ":image-transformer",
    ":uploads-service",
    ":upload-registry",
    ":upload-api",
    ":upload-services"
)

project(":cockroach-db").projectDir = file("modules/common/cockroach-db")

project(":object-storage").projectDir = file("modules/uploads_v2/object-storage")
project(":image-transformer").projectDir = file("modules/uploads_v2/image-transformer")
project(":uploads-service").projectDir = file("modules/uploads_v2/uploads-service")
project(":upload-registry").projectDir = file("modules/uploads_v2/upload-registry")
project(":upload-api").projectDir = file("modules/uploads_v2/uploads-api")
project(":upload-services").projectDir = file("modules/uploads_v2/upload-services")