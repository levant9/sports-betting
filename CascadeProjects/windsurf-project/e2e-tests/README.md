# E2E Tests

This module contains end-to-end integration tests for the sports betting system.

## Running Tests

### Regular Unit Tests (from root)
```bash
mvn test
```
This runs all unit tests in `event-api-service` and `settlement-service` modules, but **excludes** e2e tests.

### E2E Integration Tests Only
```bash
# From e2e-tests module
cd e2e-tests
mvn failsafe:integration-test

# Or from root directory
mvn failsafe:integration-test -pl e2e-tests
```

### All Tests (Unit + E2E)
```bash
mvn test failsafe:integration-test
```

## Test Configuration

The e2e tests use:
- **TestContainers** for real Kafka and PostgreSQL instances
- **Failsafe Plugin** for integration test execution
- **Spring Boot Test** with embedded application context

## Test Structure

- `*E2E.java` - End-to-end integration tests
- `*IT.java` - Integration tests (following Maven conventions)

## Dependencies

The e2e module requires:
- Docker (for TestContainers)
- All service modules to be compiled first
