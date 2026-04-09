# Minerva Core

This project was refactored to a DDD-oriented Spring Boot REST API.

## Architecture

The codebase is organized by bounded context and layers:

- `catalog`
  - `article`
  - `tax`
- `inventory`
  - `item`
  - `location`
- `identity`
  - `user`
- `purchasing`
  - `provider`
  - `purchase`
- `sales`
  - `sale`
- `shared`
  - cross-cutting rules, exception handling, and persistence helpers

Every bounded context follows the same structure:

- `domain`: domain model and repository ports
- `application`: use cases and orchestration services
- `infrastructure`: JPA entities and Spring Data adapters
- `interfaces`: REST controllers and HTTP DTOs

## Main design decisions

- Aggregates are referenced across contexts by `UUID` instead of direct object graphs.
- Domain models are immutable records with simple invariants.
- **Lombok usage**. Don't use manual getters / setters
- **No inline literals**
- Persistence is isolated behind repository ports.
- REST DTOs are separated from domain models.
- Purchase total cost is recalculated inside the domain model.
- SQLite is the default profile and MySQL is enabled through a dedicated profile.

## Technology stack

- Spring Boot 4.0.5
- Spring Web
- Spring Data JPA
- Bean Validation
- SQLite with Hibernate community dialect
- MySQL connector kept ready for production migration

## Run with SQLite

```bash
./mvnw spring-boot:run
```

By default the application uses:

- profile: `sqlite`
- database file: `./db/data.minerva.db`

You can override the database location:

```bash
SQLITE_DATABASE_PATH=./db/data.minerva.db ./mvnw spring-boot:run
```

## Run with MySQL

```bash
SPRING_PROFILES_ACTIVE=mysql MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DATABASE=minerva MYSQL_USER=root MYSQL_PASSWORD=root ./mvnw spring-boot:run
```

## API endpoints

### Taxes

- `POST /api/v1/taxes`
- `GET /api/v1/taxes`
- `GET /api/v1/taxes/{taxId}`
- `PUT /api/v1/taxes/{taxId}`
- `DELETE /api/v1/taxes/{taxId}`

### Articles

- `POST /api/v1/articles`
- `GET /api/v1/articles`
- `GET /api/v1/articles/{articleId}`
- `PUT /api/v1/articles/{articleId}`
- `DELETE /api/v1/articles/{articleId}`

### Locations

- `POST /api/v1/locations`
- `GET /api/v1/locations`
- `GET /api/v1/locations/{locationId}`
- `PUT /api/v1/locations/{locationId}`
- `DELETE /api/v1/locations/{locationId}`

### Providers

- `POST /api/v1/providers`
- `GET /api/v1/providers`
- `GET /api/v1/providers/{providerId}`
- `PUT /api/v1/providers/{providerId}`
- `DELETE /api/v1/providers/{providerId}`

### Users

- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{userId}`
- `PUT /api/v1/users/{userId}`
- `DELETE /api/v1/users/{userId}`

### Items

- `POST /api/v1/items`
- `GET /api/v1/items`
- `GET /api/v1/items/{itemId}`
- `PUT /api/v1/items/{itemId}`
- `DELETE /api/v1/items/{itemId}`

### Purchases

- `POST /api/v1/purchases`
- `GET /api/v1/purchases`
- `GET /api/v1/purchases/{purchaseId}`
- `PUT /api/v1/purchases/{purchaseId}`
- `DELETE /api/v1/purchases/{purchaseId}`

### Sales

- `POST /api/v1/sales`
- `GET /api/v1/sales`
- `GET /api/v1/sales/{saleId}`
- `PUT /api/v1/sales/{saleId}`
- `DELETE /api/v1/sales/{saleId}`

## Example payloads

### Create tax

```json
{
  "description": "VAT 21%",
  "rate": 21.0000
}
```

### Create article

```json
{
  "name": "Gaming Laptop",
  "code": "LAP-001",
  "image": "https://cdn.example.com/laptop.png",
  "description": "14-inch performance laptop",
  "taxId": "00000000-0000-0000-0000-000000000001",
  "basePrice": 1000.00,
  "retailPrice": 1210.00,
  "canHaveChildren": false,
  "numberOfChildren": 0
}
```

### Create purchase

```json
{
  "code": "PUR-2026-0001",
  "providerId": "00000000-0000-0000-0000-000000000002",
  "lines": [
    {
      "itemId": "00000000-0000-0000-0000-000000000003",
      "taxId": "00000000-0000-0000-0000-000000000001",
      "buyPrice": 650.00
    }
  ]
}
```

## Notes

- The original project was a pure model library. It is now a runnable HTTP API.
- The default persistence is file-based SQLite for local development.
- The MySQL profile is intentionally ready but disabled by default.
