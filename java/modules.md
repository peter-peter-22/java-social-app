# Notes about modules

The codes are organized in Gradle modules for easier interpretation and faster builds.

#### Module separation strategy
A domain of functions that export only business logic 
and can be effectively tested independently.

#### Plugin artifact names
To define plugins in the build conventions, we must add their artifact id and version to the root build.gradle.
The artifact names can be found on this site:
[Gradle plugin portal](https://plugins.gradle.org/)

#### Useful commands
* ```./gradlew -q run``` Run all main functions.
* ```./gradlew -q projects``` List all projects in the build.
* ```./gradlew -q build``` Build all changed modules.
* ```./gradlew :app:tasks``` List tasks of the "app" module. Optional "--all" tag displays more tasks.
* ```./gradlew :app:run``` Run the "app" module.
* ```./gradlew :spring_boot_app:bootRun``` Run the "spring_boot_app" module with spring boot.

#### The version catalog
Defining the versions in buildSrc is prioritized, 
the version catalog is used only to define the version of the
dependencies that are imported from the modules.

#### The spring modulith package.
This is for preventing the usage of some spring beans outside a folder. 
It's not for Gradle modules and it's not used in this project.

#### Relevant links
- [Gradle buildSrc guide](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
- [Spring boot multi module guide](https://spring.io/guides/gs/multi-module)