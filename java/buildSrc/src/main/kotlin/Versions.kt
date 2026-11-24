/**
 * The single home of the dependency versions this build pins by hand. Libraries whose versions the
 * Spring Boot BOM manages are declared without a version in the Spring conventions; the pins here
 * are only for libraries no BOM owns: the test stack of the plain Java modules and one-off tools
 * such as ArchUnit.
 */
object Versions {
    /** The test stack of the plain Java modules; the Spring modules resolve these from the BOM. */
    const val junitJupiter = "5.11.3"
    const val assertj = "3.27.7"
    const val mockito = "5.23.0"

    /** Compile-time tooling, used by every module. */
    const val lombok = "1.18.46"

    /** The architecture tests. */
    const val archunit = "1.5.1"
}
