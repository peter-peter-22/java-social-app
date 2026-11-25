package com.example.media.contract;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The boundary guard of the contract (modules/media/AGENTS.md): the domain vocabulary is plain Java
 * and cannot import Spring, JPA, Minio, or Jackson, and nothing in it may point back at the
 * registry adapter.
 */
@AnalyzeClasses(packages = "com.example.media.contract")
class ArchitectureTest {

    @ArchTest
    static final ArchRule contractIsFreeOfFrameworks = noClasses()
            .that().resideInAPackage("com.example.media.contract..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "io.minio..",
                    "tools.jackson..",
                    "com.fasterxml.jackson..");

    @ArchTest
    static final ArchRule contractDoesNotDependOnTheRegistry = noClasses()
            .that().resideInAPackage("com.example.media.contract..")
            .should().dependOnClassesThat().resideInAPackage("com.example.media.registry..");
}
