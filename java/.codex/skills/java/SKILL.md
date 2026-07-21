---
name: java
description: "Apply generic Java coding conventions for implementation, refactoring, and review: use Lombok to reduce boilerplate, use JSpecify for null-safety annotations, and prioritize short, simple code."
---

# Java

Apply these conventions to generic Java implementation, refactoring, and code-review tasks.

## Guidelines

- Use Lombok to remove routine boilerplate such as accessors, constructors, builders, and value-object methods when it
  keeps the design clear.
- Use JSpecify annotations to express nullness at public and non-obvious boundaries. Preserve and improve existing
  nullness contracts rather than suppressing warnings casually.
- Prefer the shortest clear implementation. Avoid unnecessary abstractions, indirection, duplication, and ceremony.
- Follow the repository's existing Java, framework, naming, formatting, and dependency conventions when they are more
  specific.
- Keep changes focused; do not introduce Lombok or JSpecify solely for a trivial one-off unless the project already uses
  them.

## Review Checklist

- Can Lombok make repetitive code smaller without hiding important behavior?
- Are nullable and non-null values documented with JSpecify where callers need the contract?
- Is the implementation easy to read, test, and change?
- Are abstractions justified by current requirements rather than speculative reuse?
