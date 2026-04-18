# Phase 0 Foundation Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebrand the base skeleton into `message-service`, remove demo modules, add MySQL/JPA/Flyway foundation, create official messaging module skeletons, and leave the application verifiably ready for Phase 1.

**Architecture:** Implement Phase 0 as five controlled slices: rebrand/package rename, legacy module removal, persistence foundation, official module skeleton + security foundation, and delivery baseline verification. Each slice must leave the repository compiling, with tests proving the new baseline instead of the deleted demo flows.

**Tech Stack:** Java 21, Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Data JPA, Flyway, Flyway MySQL plugin, MySQL, Maven, JUnit 5, AssertJ, Docker Compose

---

## File Map

### Existing files to modify
- `pom.xml` — rename artifact and add JPA/MySQL dependencies.
- `README.md` — rewrite project purpose and local run instructions for message-service.
- `compose.yaml` — add MySQL local service and wire local environment assumptions.
- `src/main/resources/application.yml` — rename application identities and shared defaults.
- `src/main/resources/application-local.yml` — add local datasource/JPA/Flyway/Hikari settings and local docs toggles.
- `src/main/resources/application-dev.yml` — add dev datasource/JPA/Flyway/Hikari settings.
- `src/main/resources/application-prod.yml` — add prod datasource/JPA/Flyway/Hikari settings.
- `src/test/resources/application.yml` — provide isolated test datasource/config so Spring Boot tests do not depend on local MySQL being up.
- `src/main/resources/logback-spring.xml` — align logger/application naming if needed.
- `src/main/resources/db/migration/V1__init_schema.sql` — replace with timestamped init migration aligned to Phase 0 convention.
- `src/test/java/me/bchieu/base/BaseApplicationTests.java` — rename package/class and update startup expectation.
- `src/test/java/me/bchieu/base/integration/ApplicationStructureSmokeTest.java` — replace base/sample/auth assertions with message-service structure assertions.
- `src/test/java/me/bchieu/base/integration/SwaggerDocsLocalIntegrationTest.java` — rename package/class if retained.
- `src/test/java/me/bchieu/base/integration/SwaggerDocsProdIntegrationTest.java` — rename package/class if retained.
- `src/main/java/me/bchieu/base/common/response/ApiResponse.java` — move to new package and keep/adapt API envelope.
- `src/main/java/me/bchieu/base/common/exception/GlobalExceptionHandler.java` — move to new package and keep/adapt validation error mapping.
- `src/main/java/me/bchieu/base/common/config/OpenApiConfig.java` — move to new package and keep docs config.
- `src/main/java/me/bchieu/base/BaseApplication.java` — rename class/package to message-service entrypoint.
- Existing package marker classes under `src/main/java/me/bchieu/base/common/**` and `src/main/java/me/bchieu/base/infrastructure/**` — move to `me.bchieu.messaging`.

### Existing files to delete
- `src/main/java/me/bchieu/base/modules/auth/**`
- `src/main/java/me/bchieu/base/modules/sample/**`
- `src/test/java/me/bchieu/base/modules/auth/**`
- `src/test/java/me/bchieu/base/modules/sample/**`
- `src/main/resources/db/migration/V2__seed_default_data.sql`
- `src/main/resources/db/migration/V3__create_sample_item_table.sql`
- `src/main/resources/db/migration/V4__seed_sample_item.sql`

### New files to create
- `src/main/java/me/bchieu/messaging/MessageServiceApplication.java`
- `src/main/java/me/bchieu/messaging/common/config/MessagingPackageMarker.java`
- `src/main/java/me/bchieu/messaging/common/security/SecurityConfig.java`
- `src/main/java/me/bchieu/messaging/common/security/CurrentPrincipal.java`
- `src/main/java/me/bchieu/messaging/common/security/CurrentIntegrationApp.java`
- `src/main/java/me/bchieu/messaging/common/security/SecurityEndpointProperties.java`
- `src/main/java/me/bchieu/messaging/modules/identity/package-info.java`
- `src/main/java/me/bchieu/messaging/modules/identity/api/package-info.java`
- `src/main/java/me/bchieu/messaging/modules/identity/application/package-info.java`
- `src/main/java/me/bchieu/messaging/modules/identity/domain/package-info.java`
- `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/package-info.java`
- Matching `package-info.java` files for `conversation`, `message`, `media`, `integration`, `realtime`, `notification`
- `src/main/resources/db/migration/V20260418_0001__init_message_service_schema.sql`
- `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`
- `src/test/java/me/bchieu/messaging/integration/SecurityConfigurationSmokeTest.java`

---

### Task 1: Rebrand project identity and package root

**Files:**
- Modify: `pom.xml`
- Modify: `README.md`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-local.yml`
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`
- Modify: `src/main/java/me/bchieu/base/BaseApplication.java`
- Create: `src/main/java/me/bchieu/messaging/MessageServiceApplication.java`
- Delete: `src/main/java/me/bchieu/base/BaseApplication.java`
- Test: `src/test/java/me/bchieu/base/BaseApplicationTests.java`

- [ ] **Step 1: Write the failing startup rename test**

```java
package me.bchieu.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessageServiceApplicationTests {

  @Test
  void shouldLoadMessageServiceApplicationContext() {
    assertThat(MessageServiceApplication.class).isNotNull();
  }
}
```

Create this file at `src/test/java/me/bchieu/messaging/MessageServiceApplicationTests.java` before moving the application class.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=me.bchieu.messaging.MessageServiceApplicationTests test`
Expected: FAIL with `cannot find symbol MessageServiceApplication`

- [ ] **Step 3: Write minimal rebrand implementation**

Use this application entrypoint:

```java
package me.bchieu.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessageServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MessageServiceApplication.class, args);
  }
}
```

Update `pom.xml` coordinates to:

```xml
<groupId>me.bchieu</groupId>
<artifactId>message-service</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>message-service</name>
<description>Backend message service modular monolith</description>
```

Update `src/main/resources/application.yml` to:

```yaml
spring:
  application:
    name: message-service

---
spring:
  config:
    activate:
      on-profile: local
  application:
    name: message-service-local

---
spring:
  config:
    activate:
      on-profile: dev
  application:
    name: message-service-dev

---
spring:
  config:
    activate:
      on-profile: prod
  application:
    name: message-service-prod
```

Rewrite `README.md` to remove all mentions of base/sample/auth and describe this repository as the BE message-service foundation.

- [ ] **Step 4: Move remaining base packages to `me.bchieu.messaging`**

Rename every remaining Java package under:
- `src/main/java/me/bchieu/base/common/**`
- `src/main/java/me/bchieu/base/infrastructure/**`
- `src/test/java/me/bchieu/base/integration/**`

to the matching `me.bchieu.messaging` paths.

Example package rewrite:

```java
package me.bchieu.messaging.common.response;
```

- [ ] **Step 5: Run targeted rename tests**

Run: `mvn -Dtest=me.bchieu.messaging.MessageServiceApplicationTests test`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add pom.xml README.md src/main/resources/application.yml src/main/resources/application-local.yml src/main/resources/application-dev.yml src/main/resources/application-prod.yml src/main/java src/test/java
git commit -m "refactor: rebrand base skeleton to message service"
```

### Task 2: Remove legacy auth/sample flows and replace their test coverage

**Files:**
- Modify: `src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java`
- Delete: `src/main/java/me/bchieu/messaging/modules/auth/**`
- Delete: `src/main/java/me/bchieu/messaging/modules/sample/**`
- Delete: `src/test/java/me/bchieu/messaging/modules/auth/**`
- Delete: `src/test/java/me/bchieu/messaging/modules/sample/**`
- Delete: `src/main/resources/db/migration/V2__seed_default_data.sql`
- Delete: `src/main/resources/db/migration/V3__create_sample_item_table.sql`
- Delete: `src/main/resources/db/migration/V4__seed_sample_item.sql`
- Test: `src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java`

- [ ] **Step 1: Rewrite the smoke test to describe the new baseline**

Replace the old smoke test with:

```java
package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationStructureSmokeTest {

  @Test
  void shouldIncludeMessageServiceConfigurationResourcesOnClasspath() {
    assertThat(getClass().getClassLoader().getResource("application.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-local.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-dev.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-prod.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("logback-spring.xml")).isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("db/migration/V20260418_0001__init_message_service_schema.sql"))
        .isNotNull();
  }

  @Test
  void shouldIncludeCorePackageSkeletonTypes() throws ClassNotFoundException {
    assertThat(Class.forName("me.bchieu.messaging.common.config.MessagingPackageMarker"))
        .isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.common.exception.GlobalExceptionHandler"))
        .isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.common.response.ApiResponse")).isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.infrastructure.persistence.PersistencePackageMarker"))
        .isNotNull();
  }
}
```

- [ ] **Step 2: Run the smoke test to verify it fails**

Run: `mvn -Dtest=me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: FAIL because the migration file and marker class names do not exist yet

- [ ] **Step 3: Delete demo code and migrations**

Delete the legacy directories and migration files listed above. Do not leave compatibility wrappers or comments in place.

- [ ] **Step 4: Add the replacement marker class**

Create `src/main/java/me/bchieu/messaging/common/config/MessagingPackageMarker.java`:

```java
package me.bchieu.messaging.common.config;

public final class MessagingPackageMarker {

  private MessagingPackageMarker() {}
}
```

- [ ] **Step 5: Run the smoke test again**

Run: `mvn -Dtest=me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: still FAIL until Task 3 creates the timestamped migration; keep the rewritten test committed as the new contract.

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java src/main/resources/db/migration
git commit -m "refactor: remove demo auth and sample modules"
```

### Task 3: Add MySQL, JPA, Flyway, and local compose foundation

**Files:**
- Modify: `pom.xml`
- Modify: `compose.yaml`
- Modify: `src/main/resources/application-local.yml`
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`
- Delete: `src/main/resources/db/migration/V1__init_schema.sql`
- Create: `src/main/resources/db/migration/V20260418_0001__init_message_service_schema.sql`
- Create: `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`
- Create: `src/test/resources/application.yml`
- Test: `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java`

- [ ] **Step 1: Write the failing migration convention test**

Create `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`:

```java
package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

class MigrationStructureIntegrationTest {

  @Test
  void shouldExposeTimestampBasedInitialMigration() {
    URL migration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0001__init_message_service_schema.sql");

    assertThat(migration).isNotNull();
  }
}
```

- [ ] **Step 2: Run the migration tests to verify they fail**

Run: `mvn -Dtest=me.bchieu.messaging.integration.MigrationStructureIntegrationTest,me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: FAIL because the timestamped migration does not exist

- [ ] **Step 3: Add persistence dependencies and local MySQL compose service**

Add these dependencies in `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

Rewrite `compose.yaml` to:

```yaml
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: message_service
      MYSQL_USER: messaging
      MYSQL_PASSWORD: messaging
      MYSQL_ROOT_PASSWORD: root
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-time-zone=+00:00
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-u", "messaging", "-pmessaging"]
      interval: 10s
      timeout: 5s
      retries: 10
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build:
      context: .
      dockerfile: Dockerfile
    env_file:
      - .env.example
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

- [ ] **Step 4: Add datasource, UTC, and HikariCP settings**

Use this `src/main/resources/application-local.yml`:

```yaml
app:
  env: local
logging:
  level:
    root: INFO
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/message_service?useUnicode=true&characterEncoding=utf8&connectionTimeZone=UTC&serverTimezone=UTC
    username: messaging
    password: messaging
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  flyway:
    enabled: true
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

Use the same structure in `application-dev.yml` and `application-prod.yml` with environment-specific host, credentials, and larger pool values.

Create `src/test/resources/application.yml` to isolate tests from local infrastructure:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:message_service_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  flyway:
    enabled: false
```

Also add the H2 test dependency in `pom.xml`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```
```},{
- [ ] **Step 5: Replace the init migration with the timestamp convention**

Delete `V1__init_schema.sql` and create `V20260418_0001__init_message_service_schema.sql`:

```sql
CREATE TABLE schema_version_lock (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
```

This is intentionally minimal: Phase 0 needs the migration pipeline working without prematurely committing business schema.

- [ ] **Step 6: Run targeted tests**

Run: `mvn -Dtest=me.bchieu.messaging.integration.MigrationStructureIntegrationTest,me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add pom.xml compose.yaml src/main/resources/application-local.yml src/main/resources/application-dev.yml src/main/resources/application-prod.yml src/main/resources/db/migration src/test/resources/application.yml src/test/java
git commit -m "feat: add mysql and migration foundation"
```

### Task 4: Create official messaging module skeletons

**Files:**
- Create: `src/main/java/me/bchieu/messaging/modules/identity/package-info.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/api/package-info.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/application/package-info.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/domain/package-info.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/package-info.java`
- Create matching files for `conversation`, `message`, `media`, `integration`, `realtime`, `notification`
- Modify: `src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java`

- [ ] **Step 1: Extend the smoke test with module skeleton assertions**

Add this test method:

```java
@Test
void shouldExposeOfficialMessagingModules() throws ClassNotFoundException {
  assertThat(Class.forName("me.bchieu.messaging.modules.identity.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.conversation.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.message.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.media.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.integration.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.realtime.package-info")).isNotNull();
  assertThat(Class.forName("me.bchieu.messaging.modules.notification.package-info")).isNotNull();
}
```

- [ ] **Step 2: Run the smoke test to verify it fails**

Run: `mvn -Dtest=me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: FAIL because the new module packages do not exist yet

- [ ] **Step 3: Create module package skeletons**

For each module package, add `package-info.java`:

```java
@org.springframework.lang.NonNullApi
package me.bchieu.messaging.modules.identity;
```

And one matching file per layer, for example:

```java
@org.springframework.lang.NonNullApi
package me.bchieu.messaging.modules.identity.api;
```

Repeat for all listed modules and layers.

- [ ] **Step 4: Run the smoke test again**

Run: `mvn -Dtest=me.bchieu.messaging.integration.ApplicationStructureSmokeTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/modules src/test/java/me/bchieu/messaging/integration/ApplicationStructureSmokeTest.java
git commit -m "feat: add messaging module skeletons"
```

### Task 5: Add security foundation seams

**Files:**
- Create: `src/main/java/me/bchieu/messaging/common/security/SecurityConfig.java`
- Create: `src/main/java/me/bchieu/messaging/common/security/CurrentPrincipal.java`
- Create: `src/main/java/me/bchieu/messaging/common/security/CurrentIntegrationApp.java`
- Create: `src/main/java/me/bchieu/messaging/common/security/SecurityEndpointProperties.java`
- Modify: `src/main/resources/application.yml`
- Create: `src/test/java/me/bchieu/messaging/integration/SecurityConfigurationSmokeTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/SecurityConfigurationSmokeTest.java`

- [ ] **Step 1: Write the failing security seam test**

Create `src/test/java/me/bchieu/messaging/integration/SecurityConfigurationSmokeTest.java`:

```java
package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import me.bchieu.messaging.common.security.CurrentIntegrationApp;
import me.bchieu.messaging.common.security.CurrentPrincipal;
import me.bchieu.messaging.common.security.SecurityEndpointProperties;
import org.junit.jupiter.api.Test;

class SecurityConfigurationSmokeTest {

  @Test
  void shouldExposeSecurityFoundationTypes() {
    assertThat(CurrentPrincipal.class).isNotNull();
    assertThat(CurrentIntegrationApp.class).isNotNull();
    assertThat(SecurityEndpointProperties.class).isNotNull();
  }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -Dtest=me.bchieu.messaging.integration.SecurityConfigurationSmokeTest test`
Expected: FAIL because the security foundation classes do not exist

- [ ] **Step 3: Add minimal security foundation types**

Create `CurrentPrincipal.java`:

```java
package me.bchieu.messaging.common.security;

public record CurrentPrincipal(Long userId, String username) {}
```

Create `CurrentIntegrationApp.java`:

```java
package me.bchieu.messaging.common.security;

public record CurrentIntegrationApp(Long integrationAppId, String appCode) {}
```

Create `SecurityEndpointProperties.java`:

```java
package me.bchieu.messaging.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityEndpointProperties(
    List<String> publicEndpoints,
    List<String> userEndpoints,
    List<String> integrationEndpoints) {}
```

Create `SecurityConfig.java`:

```java
package me.bchieu.messaging.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityEndpointProperties.class)
public class SecurityConfig {}
```

Add to `application.yml`:

```yaml
app:
  security:
    public-endpoints:
      - /actuator/health
    user-endpoints:
      - /api/v1/**
    integration-endpoints:
      - /integration/v1/**
```

- [ ] **Step 4: Run the security test again**

Run: `mvn -Dtest=me.bchieu.messaging.integration.SecurityConfigurationSmokeTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/common/security src/main/resources/application.yml src/test/java/me/bchieu/messaging/integration/SecurityConfigurationSmokeTest.java
git commit -m "feat: add security foundation seams"
```

### Task 6: Finalize delivery baseline, docs exposure, and verification

**Files:**
- Modify: `src/main/java/me/bchieu/messaging/common/response/ApiResponse.java`
- Modify: `src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java`
- Modify: `src/main/java/me/bchieu/messaging/common/config/OpenApiConfig.java`
- Modify: `src/test/java/me/bchieu/messaging/integration/SwaggerDocsLocalIntegrationTest.java`
- Modify: `src/test/java/me/bchieu/messaging/integration/SwaggerDocsProdIntegrationTest.java`
- Modify: `src/test/java/me/bchieu/messaging/MessageServiceApplicationTests.java`
- Test: `src/test/java/me/bchieu/messaging/integration/SwaggerDocsLocalIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/SwaggerDocsProdIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/MessageServiceApplicationTests.java`

- [ ] **Step 1: Update docs exposure tests to the new package names**

Move the swagger integration tests under `me.bchieu.messaging.integration` and keep the existing expectations intact.

Example package declaration:

```java
package me.bchieu.messaging.integration;
```

- [ ] **Step 2: Run the full targeted integration suite and record failures**

Run:

```bash
mvn -Dtest=me.bchieu.messaging.MessageServiceApplicationTests,me.bchieu.messaging.integration.ApplicationStructureSmokeTest,me.bchieu.messaging.integration.MigrationStructureIntegrationTest,me.bchieu.messaging.integration.SecurityConfigurationSmokeTest,me.bchieu.messaging.integration.SwaggerDocsLocalIntegrationTest,me.bchieu.messaging.integration.SwaggerDocsProdIntegrationTest test
```

Expected: PASS without requiring `docker compose up`, because Spring Boot tests must use the isolated test datasource from `src/test/resources/application.yml`.

- [ ] **Step 3: Correct any remaining baseline mismatches in shared classes**

Keep `ApiResponse` as:

```java
package me.bchieu.messaging.common.response;

public record ApiResponse<T>(boolean success, String message, T data) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, null, data);
  }
}
```

Keep `GlobalExceptionHandler` behavior equivalent under the new package:

```java
package me.bchieu.messaging.common.exception;

import java.util.stream.Collectors;
import me.bchieu.messaging.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, message, null));
  }
}
```

- [ ] **Step 4: Run full verification**

Run: `mvn verify`
Expected: BUILD SUCCESS

If Docker is available locally, also run:

```bash
docker compose up -d mysql
docker compose ps
```

Expected: MySQL service is `healthy`

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/test/java src/main/resources
git commit -m "test: lock phase zero verification baseline"
```

## Spec Coverage Check

- Rebrand to `message-service` — covered by Task 1.
- Rename package root to `me.bchieu.messaging` — covered by Task 1.
- Remove `sample` and `auth` entirely — covered by Task 2.
- Add MySQL/JPA/Flyway foundation, including `flyway-mysql` — covered by Task 3.
- Add HikariCP baseline and UTC/utf8mb4 decisions — covered by Task 3.
- Keep Spring Boot tests independent from local MySQL availability — covered by Task 3 and Task 6.
- Switch migration naming to timestamp convention — covered by Task 3.
- Add official messaging module skeletons — covered by Task 4.
- Add security foundation seams — covered by Task 5.
- Preserve/update API envelope, exception mapping, docs exposure, and verification — covered by Task 6.

## Placeholder Scan

- No `TODO`, `TBD`, or “implement later” placeholders remain.
- Every task includes exact files, commands, and code snippets.
- Cross-task dependencies are explicit: Task 2 intentionally leaves the smoke test red until Task 3 creates the migration file.

## Type Consistency Check

- Application entrypoint is consistently `me.bchieu.messaging.MessageServiceApplication`.
- Timestamp migration filename is consistently `V20260418_0001__init_message_service_schema.sql`.
- Security seam types are consistently `CurrentPrincipal`, `CurrentIntegrationApp`, and `SecurityEndpointProperties`.
- Shared response/exception classes consistently live under `me.bchieu.messaging.common`.
