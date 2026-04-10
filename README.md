# Minerva Core

This project was refactored to a DDD-oriented Spring Boot REST API.

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
