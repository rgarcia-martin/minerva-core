# Minerva Core

Minerva Core is the back-end of a small-business **point-of-sale / retail management system**, built as a DDD-oriented Spring Boot REST API. It models the full purchase → inventory → sale lifecycle, including a parent/child packaging mechanism (e.g. a *box of pens* that gets opened on receipt and auto-split into individually sellable units).

## Prerequisites (Windows)

### 1. Install JDK 26

1. Download the JDK 26 installer from [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/).
2. Run the installer and note the installation path (e.g. `C:\Program Files\Java\jdk-26`).
3. Set the `JAVA_HOME` environment variable:
   - Open **Settings > System > About > Advanced system settings > Environment Variables**.
   - Under **System variables**, click **New**:
     - Variable name: `JAVA_HOME`
     - Variable value: `C:\Program Files\Java\jdk-26`
   - Find the `Path` variable under **System variables**, click **Edit**, and add a new entry: `%JAVA_HOME%\bin`
4. Verify in a new terminal:
   ```cmd
   java --version
   ```

### 2. Install Apache Maven

1. Download the binary zip from [Maven Downloads](https://maven.apache.org/download.cgi) (e.g. `apache-maven-3.9.9-bin.zip`).
2. Extract to a permanent location (e.g. `C:\tools\apache-maven-3.9.9`).
3. Set environment variables:
   - Under **System variables**, click **New**:
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\tools\apache-maven-3.9.9`
   - Edit the `Path` variable and add a new entry: `%MAVEN_HOME%\bin`
4. Verify in a new terminal:
   ```cmd
   mvn -version
   ```
   The output should show the Maven version and the JDK 26 path from `JAVA_HOME`.

> **Note:** This project includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), so a system-wide Maven install is optional. You can run `.\mvnw.cmd` instead of `mvn` and it will download the correct Maven version automatically.

### 3. Install IntelliJ IDEA Community Edition

1. Download the installer from [JetBrains IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (select **Community Edition**).
2. Run the installer. Recommended options during setup:
   - Add `idea` to the PATH.
   - Associate `.java` files with IntelliJ.
   - Create a desktop shortcut.
3. Open IntelliJ and configure the JDK:
   - Go to **File > Project Structure > SDKs**, click **+**, and add the JDK 26 installation path.
   - Under **File > Project Structure > Project**, set the **SDK** to JDK 26 and **Language level** to 26.
4. Open the project:
   - **File > Open** and select the `minerva-core` folder.
   - IntelliJ will detect the `pom.xml` and import as a Maven project automatically.

## Architecture

The codebase is organized by bounded context. Each context contains one or more aggregates, and every aggregate follows the same 4-layer structure:

- `domain` — immutable record models and repository ports
- `application` — use-case orchestration services
- `infrastructure` — JPA entities and Spring Data adapters (`infrastructure/security` for the password hasher)
- `interfaces/rest` — REST controllers and inline HTTP DTOs

Bounded contexts and aggregates currently implemented:

- `catalog`
  - `article` — products, supports parent/child packaging via `canHaveChildren`, `numberOfChildren`, `childArticleId`
  - `tax` — VAT rate + equivalence-surcharge rate
  - `freeconcept` — non-inventory billable items (delivery, gift wrap, photocopies, table service…)
- `inventory`
  - `item` — physical stock units (read-only via REST; created by purchases)
  - `location` — warehouses / stores / shelves
- `identity`
  - `user` — employees with roles, password hashed with PBKDF2 (`infrastructure/security/Pbkdf2PasswordHasher`)
- `purchasing`
  - `provider`
  - `purchase` — generates inventory items per unit on creation; cascades item deletion on delete
- `payment`
  - `paymentmethod` — `CASH` / `CARD` / `GATEWAY`, with optional configuration blob
- `sales`
  - `sale` — references employee, optional client, payment method, items and free concepts
  - `client` — domain record only (no REST surface yet, referenced by UUID)
- `shared` — `DomainRules` invariants helper, `NotFoundException`, `ApiExceptionHandler`

## Main design decisions

- Aggregates are referenced across contexts by `UUID` only — no direct object graphs.
- Domain models are immutable Java records with invariants validated in the compact constructor via `DomainRules` (`requireNonNull`, `requireNonBlank`, `trimToNull`, `normalizeEmail`, `requirePositiveOrZero`, `scaleMoney`, `scaleRate`).
- Field names used in error messages live as `static final FIELD_*` constants on each record — no inline literals.
- **Lombok usage**. Don't write manual getters/setters.
- **No inline literals** anywhere — define `static final` constants at the top of each class.
- State transitions return new instances (`Item.markAsSold`, `Sale.confirm/cancel`, `Purchase.markAsPaid`, `User.deactivate`).
- Persistence is isolated behind repository ports (`*Repository` interface in `domain/`, `*RepositoryAdapter` in `infrastructure/persistence/`).
- REST DTOs are inner records inside each controller (`UpsertXxxRequest`, `XxxResponse`) — no shared DTO layer.
- Purchase total cost is recalculated inside the domain via `Purchase.recalculateTotalCost()`; sale total amount is computed once in `Sale.create(...)` from line totals.
- Passwords are never stored in clear: `UserService` always pipes raw passwords through the `PasswordHasher` port (default `Pbkdf2PasswordHasher`, PBKDF2-HMAC-SHA256, 210 000 iterations, 16-byte salt, 256-bit key, encoded as `pbkdf2$<iters>$<saltB64>$<hashB64>`).
- SQLite is the default profile and MySQL is enabled through a dedicated profile.

### Domain mechanics worth knowing

- **Box-opening flow**: when an article has `canHaveChildren=true`, you can purchase a unit of it in `ItemStatus.OPENED`. `PurchaseService.createInventoryItems(...)` will then auto-create one parent item plus `numberOfChildren` child items, each at cost `parentCost / numberOfChildren` (HALF_UP, 2dp), all linked back to the parent via `parentItemId`.
- **Sale lines are XOR**: a `SaleLine` references *exactly one* of `itemId` (inventory item, quantity must be 1) or `freeConceptId` (any positive quantity). The factory methods `SaleLine.createForItem(...)` and `SaleLine.createForFreeConcept(...)` enforce this.
- **Sale deletion releases stock**: deleting a sale flips every referenced item back from `SOLD` to `AVAILABLE`.
- **Purchase deletion cascades**: deleting a purchase removes all items it generated via `originPurchaseId`.

### Error handling

`ApiExceptionHandler` (`shared/interfaces/rest/`) maps:

| Exception | HTTP status |
| --- | --- |
| `NotFoundException`, `EntityNotFoundException` | 404 Not Found |
| `IllegalArgumentException`, `ConstraintViolationException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 400 Bad Request (first field error) |
| `HttpMessageNotReadableException` | 400 Bad Request (`Malformed request body`) |
| `MethodArgumentTypeMismatchException` | 400 Bad Request (e.g. invalid UUID in path) |
| `HttpMediaTypeNotSupportedException` | 415 Unsupported Media Type |
| `HttpRequestMethodNotSupportedException` | 405 Method Not Allowed |
| `DataIntegrityViolationException` | 409 Conflict |
| anything else | 500 Internal Server Error |

All responses share the body shape `ApiErrorResponse(timestamp, status, error, message, path)`.

## Technology stack

- Spring Boot 4.0.5
- Spring Web
- Spring Data JPA
- Bean Validation (Jakarta)
- Lombok
- SQLite with Hibernate community dialect (default)
- MySQL connector available via the `mysql` profile

## Run with SQLite

```bash
./mvnw spring-boot:run
```

By default the application uses:

- profile: `sqlite`
- database file: `./db/data.minerva.db`
- root log level: `INFO` (project package `com.fractalmindstudio.minerva_core` logs at `DEBUG`)
- log file: `logs.minerva.log`

You can override the database location:

```bash
SQLITE_DATABASE_PATH=./db/data.minerva.db ./mvnw spring-boot:run
```

## Run with MySQL

```bash
SPRING_PROFILES_ACTIVE=mysql MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DATABASE=minerva MYSQL_USER=root MYSQL_PASSWORD=root ./mvnw spring-boot:run
```

## Tests

The test suite mirrors `src/main/java` and is split into four layers, all running without a Spring context:

1. **Domain record tests** (`*/domain/*Test.java`) — directly construct domain records and assert invariant enforcement and value behaviour.
2. **Application service tests** (`*/application/*ServiceTest.java`) — Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`) to mock repository ports and verify orchestration / cross-aggregate validation.
3. **Controller tests** (`*/interfaces/rest/*ControllerTest.java`) — `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build()` to exercise routing, JSON (de)serialization, validation and error mapping with the real exception handler wired in.
4. **Infrastructure adapter tests** — e.g. `Pbkdf2PasswordHasherTest` checks the encoded format and randomness across invocations.

```bash
# Run all tests
./mvnw test

# Single test class
./mvnw test -Dtest=ArticleTest

# Single test method
./mvnw test -Dtest=ArticleTest#shouldCreateArticleWithScaledPrices
```

### End-to-end functional tests (`api-tests.sh`)

`api-tests.sh` is a curl-driven acceptance harness that runs against a live server at `http://localhost:8080/api/v1`. It needs `bash` and `python3` (used to diff inventory snapshots before/after a purchase).

```bash
# In one terminal
./mvnw spring-boot:run

# In another terminal
bash api-tests.sh
```

The script is organized in 14 sections — taxes, locations, providers, articles, users, payment methods, items (read-only), purchases, sales, free concepts, the **purchase → inventory → sale flow**, the **box-opening flow**, delete cascade / cleanup, and edge cases (malformed JSON, invalid UUID, missing `Content-Type`). It exits with a `PASS / FAIL / TOTAL` summary and returns non-zero on any failure.

## API endpoints

All endpoints are rooted at `/api/v1` and return JSON. `POST` returns `201 Created`, `DELETE` returns `204 No Content`.

### Taxes — `/api/v1/taxes`

`POST` · `GET` (list) · `GET /{taxId}` · `PUT /{taxId}` · `DELETE /{taxId}`

### Articles — `/api/v1/articles`

`POST` · `GET` (list) · `GET /{articleId}` · `PUT /{articleId}` · `DELETE /{articleId}`

### Locations — `/api/v1/locations`

`POST` · `GET` (list) · `GET /{locationId}` · `PUT /{locationId}` · `DELETE /{locationId}`

### Providers — `/api/v1/providers`

`POST` · `GET` (list) · `GET /{providerId}` · `PUT /{providerId}` · `DELETE /{providerId}`

### Users — `/api/v1/users`

`POST` · `GET` (list) · `GET /{userId}` · `PUT /{userId}` · `DELETE /{userId}`

### Payment methods — `/api/v1/payment-methods`

`POST` · `GET` (list) · `GET /{paymentMethodId}` · `PUT /{paymentMethodId}` · `DELETE /{paymentMethodId}`

### Free concepts — `/api/v1/free-concepts`

`POST` · `GET` (list) · `GET /{freeConceptId}` · `PUT /{freeConceptId}` · `DELETE /{freeConceptId}`

### Items — `/api/v1/items` *(read-only)*

`GET` (list) · `GET /{itemId}`

> Items are not created or updated through REST. They are generated automatically by `POST /api/v1/purchases` and released back to `AVAILABLE` when the parent sale is deleted.

### Purchases — `/api/v1/purchases`

`POST` · `GET` (list) · `GET /{purchaseId}` · `PUT /{purchaseId}` · `DELETE /{purchaseId}`

### Sales — `/api/v1/sales`

`POST` · `GET` (list) · `GET /{saleId}` · `DELETE /{saleId}`

> Sales currently expose no `PUT`. To correct a sale, delete it (which releases its items) and create a new one.

## Example payloads

### Create tax

```json
{
  "description": "IVA General",
  "rate": 21.0,
  "surchargeRate": 5.2
}
```

### Create article (simple product)

```json
{
  "name": "Gaming Laptop",
  "code": "LAP-001",
  "barcode": "8400000012345",
  "image": "https://cdn.example.com/laptop.png",
  "description": "14-inch performance laptop",
  "taxId": "00000000-0000-0000-0000-000000000001",
  "basePrice": 1000.00,
  "retailPrice": 1210.00,
  "canHaveChildren": false,
  "numberOfChildren": 0,
  "childArticleId": null
}
```

### Create article (parent package, e.g. box of 20 pens)

```json
{
  "name": "Box of Pens (20u)",
  "code": "BOX-PEN-20",
  "barcode": "8400000020001",
  "taxId": "00000000-0000-0000-0000-000000000001",
  "basePrice": 16.00,
  "retailPrice": 30.00,
  "canHaveChildren": true,
  "numberOfChildren": 20,
  "childArticleId": "00000000-0000-0000-0000-000000000099"
}
```

### Create payment method

```json
{
  "name": "Cash",
  "type": "CASH",
  "configuration": null
}
```

### Create free concept

```json
{
  "name": "Home delivery",
  "barcode": "FC-DELIVERY",
  "price": 5.50,
  "taxId": "00000000-0000-0000-0000-000000000001",
  "description": "Standard home delivery service"
}
```

### Create purchase (auto-generates 5 inventory items)

```json
{
  "code": "PUR-2026-0001",
  "providerCode": "ALB-2026-0001",
  "providerId": "00000000-0000-0000-0000-000000000002",
  "locationId": "00000000-0000-0000-0000-000000000010",
  "deposit": false,
  "lines": [
    {
      "articleId": "00000000-0000-0000-0000-000000000003",
      "quantity": 3,
      "buyPrice": 800.00,
      "profitMargin": 25.0000,
      "taxId": "00000000-0000-0000-0000-000000000001"
    },
    {
      "articleId": "00000000-0000-0000-0000-000000000004",
      "quantity": 2,
      "buyPrice": 0.80,
      "profitMargin": 50.0000,
      "taxId": "00000000-0000-0000-0000-000000000001"
    }
  ]
}
```

### Create purchase that opens a box on receipt (box-opening flow)

Set `itemStatus: "OPENED"` and `hasChildren: true` on the line. The service will create one parent item in `OPENED` state plus `numberOfChildren` child items in `AVAILABLE` state.

```json
{
  "code": "PUR-BOX-0001",
  "providerCode": "ALB-BOX-0001",
  "providerId": "00000000-0000-0000-0000-000000000002",
  "locationId": "00000000-0000-0000-0000-000000000010",
  "lines": [
    {
      "articleId": "00000000-0000-0000-0000-000000000050",
      "quantity": 1,
      "buyPrice": 16.00,
      "profitMargin": 50.0000,
      "taxId": "00000000-0000-0000-0000-000000000001",
      "itemStatus": "OPENED",
      "hasChildren": true
    }
  ]
}
```

### Create sale (mixing inventory items and free concepts)

```json
{
  "code": "SAL-2026-0001",
  "employeeId": "00000000-0000-0000-0000-000000000020",
  "clientId": "00000000-0000-0000-0000-000000000030",
  "paymentMethodId": "00000000-0000-0000-0000-000000000040",
  "lines": [
    {
      "itemId": "00000000-0000-0000-0000-000000000111",
      "unitPrice": 1210.00,
      "taxId": "00000000-0000-0000-0000-000000000001"
    },
    {
      "freeConceptId": "00000000-0000-0000-0000-000000000200",
      "quantity": 1,
      "unitPrice": 5.50,
      "taxId": "00000000-0000-0000-0000-000000000001"
    }
  ]
}
```

`clientId` is optional (anonymous walk-in sales). Each sale line must reference *exactly one* of `itemId` or `freeConceptId`; item lines must have `quantity == 1`.

## Notes

- The product backlog (Spanish user stories) lives in `requisitos.txt`. Some stories are still pending implementation: role-based authorization enforcement, login/authentication flow, deposit purchase returns at expiry, automatic equivalence-surcharge calculation per line, automatic stock allocation by margin, client CRUD endpoints, and ticket/invoice generation.
- The original project was a pure model library; it is now a runnable HTTP API with full functional coverage in `api-tests.sh`.
- The default persistence is file-based SQLite for local development.
- The MySQL profile is intentionally ready but disabled by default.
