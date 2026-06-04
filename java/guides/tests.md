# Test guide and rules

## Goal

- Test real integration.
- Check if the excepted errors are raised.
- Simulate complete practical scenario.

## Spring boot tests

In library modules, there is no @SpringBootApplication bean is the main folder.
To enable autowiring, the same annotated class is used in the test folder.