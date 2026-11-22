pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
    }
}
rootProject.name = "java"

include(
    ":cockroach-db",
    // uploads prototype
    ":object-storage",
    ":image-transformer",
    ":uploads-service",
    ":upload-registry",
    ":upload-api",
    ":upload-services",
    // clean media service, built beside the prototype
    ":media-contract",
    ":media-registry",
    ":media-api",
    ":image-worker"
)

project(":cockroach-db").projectDir = file("modules/common/cockroach-db")

project(":image-transformer").projectDir = file("modules/uploads_v2/image-transformer")
project(":uploads-service").projectDir = file("modules/uploads_v2/uploads-service")
project(":upload-registry").projectDir = file("modules/uploads_v2/upload-registry")
project(":upload-api").projectDir = file("modules/uploads_v2/uploads-api")
project(":upload-services").projectDir = file("modules/uploads_v2/upload-services")

// clean media service, built beside the prototype
project(":object-storage").projectDir = file("modules/media/object-storage")
project(":media-contract").projectDir = file("modules/media/media-contract")
project(":media-registry").projectDir = file("modules/media/media-registry")
project(":media-api").projectDir = file("modules/media/media-api")
project(":image-worker").projectDir = file("modules/media/image-worker")