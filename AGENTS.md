# Minerva Core Agent Instructions

## Environment
- Java 26 required (pom.xml)
- Spring Boot 4.0.5, TestNG framework

## Developer Commands
- Run dev server: `./mvnw spring-boot:run`
- SQLite default: `db/data.minerva.db`, profile `sqlite`
- Override DB path: `SQLITE_DATABASE_PATH=./custom/path ./mvnw spring-boot:run`
- MySQL: `SPRING_PROFILES_ACTIVE=mysql MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=root ./mvnw spring-boot:run`
- Test all: `./mvnw test`

## Testing
- Single class: `./mvnw test -Dtest=ClassName`
- Package: `./mvnw test -Dtest=com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseTest`

## Architecture
DDD-oriented Spring Boot REST API. Bounded contexts with layers:
- `domain`: immutable record models, repository ports
- `application`: use cases, orchestration services  
- `infrastructure`: JPA entities, Spring Data adapters
- `interfaces`: REST controllers, HTTP DTOs (separate from domain)

Bounded contexts: `catalog` (article/tax), `inventory` (item/location), `identity` (user), `purchasing` (provider/purchase), `sales`, `shared`.

## Key Facts
- Aggregates reference each other by UUID only (no direct object graphs across contexts)
- Purchase total cost recalculated inside domain model
- **Lombok usage**. Don't use manual getters / setters
- **No inline literals**
- Default profile: sqlite via `spring.profiles.default=sqlite`
- REST API base path: `/api/v1/`
- Logs: `logs.minerva.log`, default level DEBUG
