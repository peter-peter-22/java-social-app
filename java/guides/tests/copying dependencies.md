# Copying all dependencies

There is a way to copy all dependencies to a test scope, but it's
currently not in use. Manually defining the implementations is the
standard.

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
