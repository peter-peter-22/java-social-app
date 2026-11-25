# Copying all dependencies

Copying all dependencies to a test fixture.

(Not wired into a build convention; add the block below to the build file of the module that needs the copy.)

## java-test-fixtures

```
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
```

## jvm-test-suite

The test name must be given with string when using the jvm-test-suite plugin.

```
configurations{
	named("integrationTestImplementation") {
		extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
	}

	named("integrationTestRuntimeOnly") {
		extendsFrom(configurations.testRuntimeOnly.get())
	}

	named("integrationTestAnnotationProcessor") {
		extendsFrom(configurations.annotationProcessor.get())
	}
	
	named("integrationTestCompileOnly") {
		extendsFrom(configurations.compileOnly.get())
	}
}
```
