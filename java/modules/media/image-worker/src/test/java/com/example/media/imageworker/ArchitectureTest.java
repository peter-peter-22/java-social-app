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
    // TODO the arch unit rules do not cover all illegal states.
    //  example: importing the media-client from image-worker, importing the image-worker from the media-api
    //  should this be extended?
    @ArchTest
    static final ArchRule workerDoesNotDependOnTheRegistry = noClasses()
            .that().resideInAnyPackage("com.example.media.imageworker..","com.example.media.video_worker..")
            .should().dependOnClassesThat().resideInAPackage("com.example.media.registry..");
}
