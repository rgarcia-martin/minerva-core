# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Memory (MANDATORY)

**At the start of every session, read `memory/MEMORY.md` and all files it references.** This is the project's version-controlled memory system and is the single source of truth for accumulated decisions, feedback, and context across sessions.

- **Use `memory/` (project root) instead of `~/.claude/projects/.../memory/`.** All memories MUST be stored in the project's `memory/` directory so they are tracked by git.
- When the user asks to remember something or gives feedback worth persisting, write it to a file in `memory/` following the frontmatter format (`name`, `description`, `type`) and add a one-line pointer in `memory/MEMORY.md`.
- Before writing a new memory, check if an existing one should be updated instead.
- When recalling or applying memories, always read from `memory/` — never from the internal Claude profile directory.

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

# Run end-to-end functional API tests against a running server (requires bash + python3)
bash api-tests.sh
```

Java version: 26. Default profile: `sqlite`. Default DB file: `./db/data.minerva.db`. Root log level is `INFO`; the project package `com.fractalmindstudio.minerva_core` logs at `DEBUG`.

## Architecture

DDD-oriented Spring Boot REST API organized by bounded context. Each context lives under `src/main/java/com/fractalmindstudio/minerva_core/` and follows a strict 4-layer structure:

```
<context>/<aggregate>/
  domain/          — immutable record models + repository port interfaces
  application/     — service classes (use-case orchestration)
  infrastructure/  — JPA entity classes + Spring Data repository adapters
  interfaces/rest/ — @RestController + request/response records (DTOs inline)
```

Bounded contexts and their aggregates:

- **catalog** — `article`, `tax`, `freeconcept` (non-stock services such as delivery, gift wrap, photocopies)
- **inventory** — `item`, `location`
- **identity** — `user` (with `infrastructure/security/Pbkdf2PasswordHasher` for password hashing)
- **purchasing** — `provider`, `purchase`
- **payment** — `paymentmethod` (CASH / CARD / GATEWAY, configurable per type)
- **sales** — `sale`, `client` (client is currently a domain-only record; cross-context references use `clientId` UUID)
- **shared** — cross-cutting (`DomainRules`, `NotFoundException`, `ApiExceptionHandler`)

### Key design rules

- **Domain models are Java records** with invariant validation in the compact constructor via `DomainRules` (`shared/domain/DomainRules.java`). Use `DomainRules.requireNonNull`, `requireNonBlank`, `trimToNull`, `normalizeEmail`, `requirePositiveOrZero`, `scaleMoney` (2dp), `scaleRate` (4dp).
- **Static `FIELD_*` constants** on every domain record name the fields used in error messages — never inline a literal string for a field name.
- **Lombok usage**. Don't write manual getters/setters. `ApiExceptionHandler` and `ArticleService` use `@Log4j2`.
- **No inline literals** — define `static final` constants at the top of each class.
- **Cross-context references use UUID only** — no object graph references between bounded contexts.
- **Factory method pattern**: domain records expose static `create(...)` (and variants such as `SaleLine.createForItem` / `createForFreeConcept`) that call `UUID.randomUUID()` and apply defaults. Direct constructor use is for reconstruction from persistence and for state transitions.
- **State transitions return new instances**: `Item.markAsSold()` / `markAsAvailable()`, `Sale.confirm()` / `cancel()`, `Purchase.markAsPaid()`, `User.deactivate()`.
- **Repository ports** are interfaces in `domain/`; adapters in `infrastructure/persistence/` implement them using Spring Data JPA (`SpringData*Repository` extends `JpaRepository`).
- **DTOs are inner records** inside the controller class (`UpsertXxxRequest`, `XxxResponse`). No shared DTO layer.
- **`Purchase.totalCost`** is recalculated inside the domain via `recalculateTotalCost()` — never set manually. `Sale.totalAmount` is computed once in `Sale.create(...)` from line totals.

### Aggregate-specific notes

- **Article**: supports a recursive parent/child packaging tree via `List<ArticleChild> children`, where each `ArticleChild(childArticleId, quantity)` specifies a child article and how many units the parent contains. `canHaveChildren()` is derived from `!children.isEmpty()`. Children can have their own children recursively (no depth limit). Cycle detection runs at save time in `ArticleService` via BFS traversal. The compact constructor enforces no self-reference and no duplicate child IDs.
- **Item**: now read-only via REST (`ItemController` exposes only `GET /api/v1/items` and `GET /api/v1/items/{id}`). Items are *only* created by `PurchaseService.create(...)` — there is no manual item POST endpoint. Each item carries `originPurchaseId` so deleting a purchase cascades to its generated stock (`itemRepository.deleteAllByOriginPurchaseId`).
- **Purchase → inventory expansion**: `PurchaseService.createInventoryItems(...)` generates one `Item` per unit in each `PurchaseLine`. When the purchased article `canHaveChildren` and the line uses `ItemStatus.OPENED`, `createDescendantItems(...)` recursively traverses the full article genealogical tree, creating items for all descendants. At each level, the parent item's cost is divided equally among total child units (`sum of all children quantities`), rounded HALF_UP to 2dp. Each child item is linked back via `parentItemId`. Mid-level items can be both parent (`hasChildren=true`) and child (`parentItemId!=null`). This is the "box-opening" flow.
- **PurchaseLine**: `quantity` must be > 0, `profitMargin` is scaled to 4dp, defaults `itemStatus` to `AVAILABLE` and `hasChildren` to false. `lineTotal()` = `buyPrice × quantity` scaled to 2dp.
- **Sale**: `paymentMethodId` is required, `clientId` is optional (anonymous walk-in sales allowed), `state` defaults to `NEW`. `SaleService.create(...)` validates the employee user and payment method exist, validates each line's tax/item/freeConcept references, then marks any referenced items as `SOLD`.
- **SaleLine**: must reference *exactly one* of `itemId` or `freeConceptId` (XOR). When selling an inventory `itemId`, `quantity` must equal 1; free-concept lines may have any positive quantity. Use `SaleLine.createForItem(...)` and `SaleLine.createForFreeConcept(...)`.
- **Sale deletion**: `SaleService.delete(...)` releases each sold item back to `AVAILABLE` via `releaseSoldItems(...)` before removing the sale.
- **PaymentMethod**: enum-typed (`PaymentMethodType.CASH | CARD | GATEWAY`) with an optional free-text `configuration` blob (e.g., gateway credentials). Full CRUD at `/api/v1/payment-methods`.
- **FreeConcept**: catalog entry for non-inventory billable items (delivery, gift wrap, photocopies). Has `barcode`, `price`, `taxId`, `description`. Full CRUD at `/api/v1/free-concepts`.
- **User**: `UserService.create/update` always hashes the raw password through the injected `PasswordHasher` port before persisting `passwordHash`. The default adapter is `Pbkdf2PasswordHasher` (PBKDF2WithHmacSHA256, 210 000 iterations, 16-byte salt, 256-bit key, encoded as `pbkdf2$<iters>$<saltB64>$<hashB64>`). Email is normalized to lowercase, roles must be non-empty, and `User.deactivate()` returns a deactivated copy.
- **Client** (sales context): exists as a domain record (`name`, `lastName`, optional `email`/`phone`/`taxIdentifier`) but currently has no service/controller wired up — sales reference clients only by UUID.

### Error handling

`ApiExceptionHandler` (`shared/interfaces/rest/`) maps:

- `NotFoundException`, `EntityNotFoundException` → 404
- `IllegalArgumentException`, `ConstraintViolationException` → 400
- `MethodArgumentNotValidException` → 400 (first field error, formatted `field: message`)
- `HttpMessageNotReadableException` → 400 (`Malformed request body`)
- `MethodArgumentTypeMismatchException` → 400 (e.g., invalid UUID in path)
- `HttpMediaTypeNotSupportedException` → 415
- `HttpRequestMethodNotSupportedException` → 405
- `DataIntegrityViolationException` → 409
- everything else → 500

All responses use `ApiErrorResponse(timestamp, status, error, message, path)`.

### Persistence profiles

- `sqlite` (default): `application.properties` + `application-sqlite.properties`, uses Hibernate community SQLite dialect.
- `mysql`: activate with `SPRING_PROFILES_ACTIVE=mysql`, configured via `application-mysql.properties` using env vars `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.

### Tests

Tests live in `src/test/` mirroring the main package structure. There are now three layers of unit tests, all without a Spring context:

1. **Domain record tests** (`*/domain/*Test.java`) — directly construct domain records and assert invariant enforcement, scaling, and value behaviour.
2. **Application service tests** (`*/application/*ServiceTest.java`) — use Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`) to mock repository ports and verify orchestration / cross-aggregate validation. Examples: `PurchaseServiceTest`, `SaleServiceTest`, `UserServiceTest`, `ArticleServiceTest`, `PaymentMethodServiceTest`.
3. **Controller tests** (`*/interfaces/rest/*ControllerTest.java`) — use `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build()` to exercise HTTP routing, JSON (de)serialization, validation, and error mapping with the real `ApiExceptionHandler` wired in. Mock the service with `@Mock`/`@InjectMocks`.
4. **Infrastructure adapter tests** — `Pbkdf2PasswordHasherTest` instantiates the real hasher and asserts the format `pbkdf2$<iters>$<salt>$<hash>` plus randomness across invocations.

When adding a new aggregate, mirror this layering: domain test → service test (mocked repos) → controller test (MockMvc + `ApiExceptionHandler`).

### Functional API tests — `api-tests.sh`

`api-tests.sh` is a curl-driven end-to-end harness against `http://localhost:8080/api/v1`. Run it after starting the server with `bash api-tests.sh`. It needs `python3` for JSON parsing and `bash` (mapfile / process substitution).

Helpers: `assert_status`, `assert_contains`, `assert_not_contains`, `assert_count`, `extract_id`, `extract_all_ids`, `extract_new_item_ids` (Python diff of items before/after a purchase), `do_post|do_get|do_put|do_delete`, `parse_response` (splits body and trailing HTTP code).

Section layout:

1. TAXES — CRUD + validation + 404
2. LOCATIONS — CRUD + validation
3. PROVIDERS — CRUD + surcharge toggle
4. ARTICLES — CRUD + tax reference
5. USERS — CRUD with roles
6. PAYMENT METHODS — CRUD across `CASH` / `CARD` / `GATEWAY`
7. ITEMS (read-only) — only `GET /items` and `GET /items/{id}`
8. PURCHASES — CRUD; verifies inventory is auto-generated
9. SALES — CRUD; mixes item lines and free-concept lines
10. FREE CONCEPTS — CRUD
11. **PURCHASE → INVENTORY → SALE flow** — creates a multi-line purchase, resolves the new items via `extract_new_item_ids`, verifies all items are `AVAILABLE`, creates two sales (one anonymous, one with `clientId`) mixing items and free concepts, asserts items transition to `SOLD`.
12. **BOX-OPENING flow** — creates a parent "Box of Pens (20u)" article with `canHaveChildren=true` referencing a child "Pen (unit)" article, purchases the box in `OPENED` state, asserts the parent and 20 child items are auto-generated, then sells units.
13. DELETE cascade / cleanup — deletes free concepts, sales (asserts sold items are released to `AVAILABLE`), purchases (asserts originated items are removed), then payment methods, articles, users, providers, locations, taxes.
14. EDGE CASES — malformed JSON → 400, invalid UUID in path → 400, missing `Content-Type` → 415.

The script tracks `PASS` / `FAIL` / `TOTAL` and exits non-zero on any failure. When changing controller routes, request/response shapes, or domain invariants, update the relevant section here in addition to the unit tests.
