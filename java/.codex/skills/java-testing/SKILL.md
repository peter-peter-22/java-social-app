---
name: java-testing
description: Write and maintain Java unit and integration tests using JUnit 5, AssertJ, Mockito, Spring MockMvc, SpringBootTest, and MockWebServer. Use when creating, updating, or reviewing tests for Java or Spring Boot code, including service tests, controller tests, application-context wiring tests, and HTTP-client tests.
---

# Java Testing

## Workflow

1. Inspect the production code, existing tests, build configuration, and test conventions before writing tests.
2. Choose the smallest test type that verifies the behavior:
    - Use a JUnit 5 unit test for isolated domain or service logic.
    - Use a Spring `MockMvc` test for web endpoint behavior.
    - Use `@SpringBootTest` when Spring Boot wiring, configuration, or multiple application components must be verified.
    - Use MockWebServer when testing code that makes HTTP requests to an external service.
3. Cover observable behavior, including success, validation, failure, and boundary cases relevant to the change.
4. Keep tests deterministic and independent. Avoid real network calls, sleeps, shared mutable state, and unnecessary
   application-context startup.
5. Run the focused test class first, then the relevant broader test suite.

## Rules

- Use JUnit 5 (`org.junit.jupiter`) for all test lifecycle annotations and test structure.
- Use AssertJ for assertions. Prefer fluent assertions such as `assertThat(actual).isEqualTo(expected)` and exception
  assertions with `assertThatThrownBy`.
- Use Mockito for mocks, stubs, and interaction verification. Mock collaborators at the system boundary; do not mock the
  class under test.
- Use `MockMvc` to exercise Spring MVC endpoints without starting a real HTTP server. Assert status, headers, content,
  and meaningful JSON fields.
- Use `@SpringBootTest` for Spring Boot wiring and integration behavior. Keep the context narrow when a slice test is
  sufficient.
- Use MockWebServer to provide deterministic HTTP responses and verify outbound requests. Shut it down reliably and
  avoid depending on external services.
- Follow the repository's existing package, naming, fixture, and dependency conventions. Use Lombok and JSpecify when
  the project rules require them, including in test helpers and fixtures.
- Prefer short, focused tests with clear arrange/act/assert structure. Use parameterized tests when multiple inputs
  share the same behavior.

## Typical Pattern

```java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository repository;
    @InjectMocks
    UserService service;

    @Test
    void findsUser() {
        given(repository.findById(1L)).willReturn(Optional.of(new User(1L, "Ada")));

        assertThat(service.find(1L).name()).isEqualTo("Ada");
        then(repository).should().findById(1L);
    }
}
```

For Spring tests, select the least expensive appropriate setup, use `MockMvc` for controller behavior, and reserve
`@SpringBootTest` for wiring or integration coverage. For HTTP clients, enqueue MockWebServer responses and assert the
recorded request method, path, headers, and body as well as the client result.
