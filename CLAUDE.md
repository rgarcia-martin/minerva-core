# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application (SQLite, default)
./mvnw spring-boot:run

# Run with MySQL profile
SPRING_PROFILES_ACTIVE=mysql MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DATABASE=minerva MYSQL_USER=root MYSQL_PASSWORD=root ./mvnw spring-boot:run

# Build (also runs tests)
./mvnw package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ArticleTest

# Run a single test method
./mvnw test -Dtest=ArticleTest#shouldCreateArticleWithScaledPrices
```

Java version: 26. Default profile: `sqlite`. Default DB file: `./db/data.minerva.db`.

## Architecture

DDD-oriented Spring Boot REST API organized by bounded context. Each context lives under `src/main/java/com/fractalmindstudio/minerva_core/` and follows a strict 4-layer structure:

```
<context>/<aggregate>/
  domain/          — immutable record models + repository port interfaces
  application/     — service classes (use-case orchestration)
  infrastructure/  — JPA entity classes + Spring Data repository adapters
  interfaces/rest/ — @RestController + request/response records (DTOs inline)
```

Bounded contexts: `catalog` (article, tax), `inventory` (item, location), `identity` (user), `purchasing` (provider, purchase), `sales` (sale), `shared` (cross-cutting).

### Key design rules

- **Domain models are Java records** with invariant validation in the compact constructor via `DomainRules` (`shared/domain/DomainRules.java`). Use `DomainRules.requireNonNull`, `requireNonBlank`, `requirePositiveOrZero`, `scaleMoney` (2dp), `scaleRate` (4dp).
- **Lombok usage**. Don't use manual getters / setters
- **No inline literals**
- **Cross-context references use UUID only** — no object graph references between bounded contexts.
- **Factory method pattern**: domain records have a static `create(...)` method that calls `UUID.randomUUID()` and applies defaults. Direct constructor use is for reconstruction from persistence.
- **Repository ports** are interfaces in `domain/`; adapters in `infrastructure/persistence/` implement them using Spring Data JPA (`SpringData*Repository` extends `JpaRepository`).
- **DTOs are inner records** inside the controller class (`UpsertXxxRequest`, `XxxResponse`). No shared DTO layer.
- **`Purchase.totalCost`** is recalculated inside the domain via `recalculateTotalCost()` — never set manually.

### Error handling

`ApiExceptionHandler` (`shared/interfaces/rest/`) maps: `NotFoundException` → 404, `IllegalArgumentException` / `ConstraintViolationException` → 400, `MethodArgumentNotValidException` → 400 (first field error), everything else → 500.

### Persistence profiles

- `sqlite` (default): `application.properties` + `application-sqlite.properties`, uses Hibernate community SQLite dialect.
- `mysql`: activate with `SPRING_PROFILES_ACTIVE=mysql`, configured via `application-mysql.properties` using env vars `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.

### Tests

Tests live in `src/test/` mirroring the main package structure. Current tests are pure unit tests on domain records — no Spring context, no mocks, no DB. They directly construct domain objects and assert invariant enforcement and value behaviour.
