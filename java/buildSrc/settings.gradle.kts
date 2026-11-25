// buildSrc is a separate build, so it imports the same catalog file the root build uses.
// This keeps every pinned dependency and plugin version in one place: gradle/libs.versions.toml.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
