# Notes about modules

The codes are organized in Gradle modules for easier interpretation and faster builds.

#### Module separation strategy
The modules implement the ports of the domain.
Some other modules define shared but not domain-specific logic.
So, the module types are:
- domain (java only)
- adapter (port implementations and applications)
- shared infrastructure (no spring modules here if possible to avoid complex scanning rules (there are exceptions, e.g., the cockroachdb module))

#### Plugin artifact names
To use plugins in the build conventions, we must add their artifact to `buildSrc/build.gradle.kts` with a version from the version catalog.
The artifact names can be found on this site:
[Gradle plugin portal](https://plugins.gradle.org/)

#### Useful commands
* ```./gradlew -q run``` Run all main functions.
* ```./gradlew -q projects``` List all projects in the build.
* ```./gradlew -q build``` Build all changed modules.
* ```./gradlew :app:tasks``` List tasks of the "app" module. Optional "--all" tag displays more tasks.
* ```./gradlew :app:run``` Run the "app" module.
* ```./gradlew :spring_boot_app:bootRun``` Run the "spring_boot_app" module with spring boot.

#### Dependency versions
The version catalog (`gradle/libs.versions.toml`) is the single home of the dependency and plugin versions.
Libraries whose versions the Spring Boot BOM manages are declared without a version; the catalog pins only what no BOM owns.

`buildSrc` is a separate build and imports the same file (`buildSrc/settings.gradle.kts`), so the conventions and `buildSrc/build.gradle.kts` read from the one catalog.
The module scripts and `buildSrc/build.gradle.kts` use the type-safe `libs.*` accessors.
Gradle does not generate those accessors inside precompiled script plugins, so the convention scripts look the entries up by name (`VersionCatalogsExtension`).

#### The spring modulith package
This is for preventing the usage of some spring beans outside a folder. 
It's not for Gradle modules and it's not used in this project.

#### Relevant links
- [Gradle buildSrc guide](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
- [Gradle version catalog guide](https://docs.gradle.org/current/userguide/version_catalogs.html)
- [Spring boot multi module guide](https://spring.io/guides/gs/multi-module)