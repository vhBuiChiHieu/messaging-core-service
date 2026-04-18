# Message Service Foundation

Java 21 + Spring Boot 3.5.x backend foundation for the `message-service` modular monolith.

## Project structure
- `common/`: shared cross-cutting components.
- `infrastructure/`: shared technical adapters.
- `modules/`: business modules.

## Run locally
```bash
mvn spring-boot:run
```

## Verify locally
```bash
mvn verify
```

This runs compile, tests, and code quality checks.

## Documentation
- `docs/architecture.md`
- `docs/api-convention.md`
- `docs/database-convention.md`
- `docs/error-code.md`
