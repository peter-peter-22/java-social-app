package com.example.media.client;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The boundary guard of the client (modules/media/AGENTS.md): it talks HTTP through the shared
 * media JSON shape and never touches the server internals — no registry, no application services,
 * no worker, no object storage.
 */
@AnalyzeClasses(packages = "com.example.media.client")
class ArchitectureTest {

    @ArchTest
    static final ArchRule clientDoesNotDependOnServerInternals = noClasses()
            .that().resideInAPackage("com.example.media.client..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.example.media.registry..",
                    "com.example.media.api..",
                    "com.example.media.imageworker..",
                    "com.example.object_storage..");
}
