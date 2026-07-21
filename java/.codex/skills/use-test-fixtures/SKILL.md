---
name: use-test-fixtures
description: Reuse repository and unit-test utilities from Java test fixtures when writing or changing tests that create domain data, prepare inserts, persist entities, or configure Spring test contexts. Use when working in a module that owns or imports testFixtures, especially utilities named Test*Creator, Test*Persistence, or Test*PersistenceConfiguration.
---

# Use Test Fixtures

Prefer the repository's test fixtures for creating valid data and interacting with persistence. They keep tests short,
consistent with module defaults, and resilient to changes in constructors or required fields.

## Workflow

1. Inspect the edited module's `build.gradle.kts` before writing test setup. Identify its own `src/testFixtures` and
   every imported fixture dependency declared with `testFixtures(project(...))`. Follow those dependencies transitively
   when a helper needs a related entity, such as an upload requiring a user.
2. Search those fixture source sets for helpers. Look for `Test*Creator`, `Test*Persistence`, and
   `Test*PersistenceConfiguration`, plus nearby classes whose names describe the data type. Read the helper before using
   it; do not guess its defaults or method signatures.
3. For unit/API tests, use a `Test*Creator` to build the main data object. Prefer its no-argument factory for ordinary
   cases and its `Consumer<...Builder>` customizer for the few fields relevant to the test. Use specialized factories
   such as `createImage()` or `createUploadFromPath(...)` when they express intent.
4. For repository/integration tests, use a `Test*Persistence` helper for inserts. Prefer its no-argument insert, then
   customize only the inserted entity fields needed by the scenario. If the helper inserts prerequisites, let it do so
   rather than manually creating prerequisite rows.
5. When a persistence helper is used as a Spring bean, import the corresponding `Test*PersistenceConfiguration`. Check
   that it scans the repository package and imports the helper and its prerequisite helpers. Reuse an existing
   application/test configuration when it already imports this configuration.
6. Keep fixture imports explicit (or use the project's established static-import style). Verify the test fixture is
   available to the edited source set; if it is not, update the module's test dependency only when that dependency is
   part of the requested change.

## Selection rules

- Use creators for in-memory domain objects passed to services, transformations, filters, or controller tests.
- Use persistence helpers for repository-backed state and IDs returned by repository inserts.
- Use both when needed: a persistence helper may create prerequisite users while a creator supplies an independent API
  object for the assertion.
- Prefer fixture customization over duplicating builder defaults. Do not hand-build a data class merely to replace a
  fixture that already covers the type.
- Do not use a persistence helper for a test specifically about repository mapping, validation, uniqueness, or failure
  behavior when the setup must expose the exact raw insert; use the helper's insert-builder preparation method if
  available, or construct the minimal raw input deliberately.
- If no suitable fixture exists, add the smallest reusable fixture only when the task calls for test infrastructure;
  otherwise document why direct construction is necessary.

## Repository-specific patterns

The users and uploads modules are reference implementations:

- `users-api` exposes `TestUserCreator` for unit-test data.
- `users-persistence` exposes `TestUserPersistence` and `TestUserPersistenceConfiguration`.
- `uploads-api` exposes `TestUploadCreator` and `TestTransformationCreator`.
- `uploads-persistence` exposes `TestUploadPersistence` and `TestUploadPersistenceConfiguration`; its persistence helper
  composes `TestUserPersistence` for required users.

Use these as patterns, not as assumptions that every module has identical method names. The source code and Gradle
fixture dependencies are authoritative.
