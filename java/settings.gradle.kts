rootProject.name = "java"

include(
    // common
    ":cockroach-db",

    // media service
    ":object-storage",
    ":media-contract",
    ":media-json",
    ":media-registry",
    ":media-api",
    ":media-client",
    ":image-worker",
    ":video-worker"
)

// common
project(":cockroach-db").projectDir = file("modules/common/cockroach-db")

// media service
project(":object-storage").projectDir = file("modules/media/object-storage")
project(":media-contract").projectDir = file("modules/media/media-contract")
project(":media-json").projectDir = file("modules/media/media-json")
project(":media-registry").projectDir = file("modules/media/media-registry")
project(":media-api").projectDir = file("modules/media/media-api")
project(":media-client").projectDir = file("modules/media/media-client")
project(":image-worker").projectDir = file("modules/media/image-worker")
project(":video-worker").projectDir = file("modules/media/video-worker")