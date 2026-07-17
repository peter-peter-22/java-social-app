// copy dependencies to the test fixture

plugins {
    java
    `java-test-fixtures`
}

configurations {
    testFixturesImplementation {
        extendsFrom(implementation.get())
    }

    testFixturesRuntimeOnly {
        extendsFrom(testRuntimeOnly.get())
    }

    testFixturesCompileOnly {
        extendsFrom(compileOnly.get())
    }

    testFixturesAnnotationProcessor {
        extendsFrom(annotationProcessor.get())
    }
}
