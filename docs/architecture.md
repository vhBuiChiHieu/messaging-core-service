# Architecture

## Target architecture
This project is a modular monolith implemented as a **single deployable Spring Boot service**.

## Source layout
- `modules/`: business modules, each module owns its API, application, domain, and infrastructure packages.
- `common/`: cross-cutting shared code (for example shared response, validation, security utilities).
- `infrastructure/`: shared technical adapters reused by modules (for example persistence, messaging, cache, storage, logging).

## Dependency rules
1. Layer flow inside a module:
   - `api -> application -> domain`
2. Shared infrastructure dependency direction:
   - `infrastructure -> domain` **or** `infrastructure -> application`
3. `common` must not depend on any package inside `modules`.
4. A module must not directly depend on another module's `infrastructure` package.

## Practical guidance
- Keep business logic in `domain` and `application`, not in controllers.
- Keep module boundaries explicit; share only stable abstractions through `common` or shared `infrastructure`.

## Base responsibilities
- This base is a product-ready minimal starter, not a full platform.
- The repository ships with lightweight quality gates in local `mvn verify` and CI.
- `modules/sample` is a reference implementation that may be removed or replaced after cloning.
