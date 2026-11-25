package com.example.media.imageworker;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The boundary guard of the worker (modules/media/AGENTS.md): the worker transforms files and moves
 * them through object storage; it cannot import the registry adapter.
 */
@AnalyzeClasses(packages = "com.example.media.imageworker")
class ArchitectureTest {

    @ArchTest
    static final ArchRule workerDoesNotDependOnTheRegistry = noClasses()
            .that().resideInAPackage("com.example.media.imageworker..")
            .should().dependOnClassesThat().resideInAPackage("com.example.media.registry..");
}
