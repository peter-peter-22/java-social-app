package com.example.media.json;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The boundary guard of the JSON shape (modules/media/AGENTS.md): media-json cannot import Spring,
 * JPA, or Minio, and nothing in it may point back at the registry adapter.
 */
@AnalyzeClasses(packages = "com.example.media.json")
class ArchitectureTest {

    @ArchTest
    static final ArchRule jsonIsFreeOfFrameworks = noClasses()
            .that().resideInAPackage("com.example.media.json..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "io.minio..");

    @ArchTest
    static final ArchRule jsonDoesNotDependOnTheRegistry = noClasses()
            .that().resideInAPackage("com.example.media.json..")
            .should().dependOnClassesThat().resideInAPackage("com.example.media.registry..");
}
