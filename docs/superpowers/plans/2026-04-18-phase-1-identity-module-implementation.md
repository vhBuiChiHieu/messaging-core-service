# Phase 1 Identity Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first working Phase 1 identity slice with MySQL-backed `app_user` storage, internal create/update/query APIs, and integration tests for the approved business rules.

**Architecture:** Implement a vertical slice inside `modules/identity` using the repo’s modular monolith structure: API for request/response mapping, application for orchestration, domain for business rules, and infrastructure for JPA persistence. Keep `username` uniqueness and immutability in the application/domain boundary, keep optimistic locking internal via `@Version`, and reuse `ApiResponse` plus `GlobalExceptionHandler` for consistent responses.

**Tech Stack:** Java 21, Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Data JPA, Flyway, MySQL/H2 test profile, JUnit 5, AssertJ, MockMvc.

---

## File Structure

**Create:**
- `src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql` — creates `app_user` table and unique index/constraint for `username`
- `src/main/java/me/bchieu/messaging/modules/identity/domain/UserStatus.java` — identity user status enum
- `src/main/java/me/bchieu/messaging/modules/identity/domain/User.java` — domain model for internal user data
- `src/main/java/me/bchieu/messaging/modules/identity/domain/UserRepository.java` — domain-facing repository contract
- `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserAlreadyExistsException.java` — conflict exception for duplicate username
- `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserNotFoundException.java` — not-found exception for missing user
- `src/main/java/me/bchieu/messaging/modules/identity/application/CreateUserCommand.java` — create use case input
- `src/main/java/me/bchieu/messaging/modules/identity/application/UpdateUserCommand.java` — update use case input
- `src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java` — application service for create/update/query flows
- `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaEntity.java` — JPA mapping for `app_user`
- `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaRepository.java` — Spring Data repository
- `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/JpaUserRepository.java` — domain repository implementation
- `src/main/java/me/bchieu/messaging/modules/identity/api/CreateUserRequest.java` — create request DTO
- `src/main/java/me/bchieu/messaging/modules/identity/api/UpdateUserRequest.java` — update request DTO
- `src/main/java/me/bchieu/messaging/modules/identity/api/UserResponse.java` — response DTO returned in API body
- `src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java` — internal REST endpoints for identity slice
- `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java` — integration coverage for create/update/query API flows
- `src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java` — integration coverage for migration/repository/version baseline

**Modify:**
- `src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java` — add handlers for duplicate username and missing user
- `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java` — assert new Flyway migration is present

---

### Task 1: Add the `app_user` migration baseline

**Files:**
- Create: `src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql`
- Test: `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

Modify `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java` to assert both migration files exist:

```java
package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

class MigrationStructureIntegrationTest {

  @Test
  void shouldExposeTimestampBasedInitialMigration() {
    URL initialMigration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0001__init_message_service_schema.sql");
    URL identityMigration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0002__create_app_user_table.sql");

    assertThat(initialMigration).isNotNull();
    assertThat(identityMigration).isNotNull();
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=MigrationStructureIntegrationTest test`
Expected: FAIL because `V20260418_0002__create_app_user_table.sql` does not exist yet.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql`:

```sql
CREATE TABLE app_user (
    id BINARY(16) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -Dtest=MigrationStructureIntegrationTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java
git commit -m "feat: add app user migration"
```

### Task 2: Prove the persistence mapping with a repository-level integration test

**Files:**
- Create: `src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java`:

```java
package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.UserStatus;
import me.bchieu.messaging.modules.identity.infrastructure.persistence.UserJpaEntity;
import me.bchieu.messaging.modules.identity.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class IdentityUserPersistenceIntegrationTest {

  @Autowired private UserJpaRepository userJpaRepository;

  @Test
  void shouldPersistUserWithInitialVersion() {
    UserJpaEntity entity =
        new UserJpaEntity(
            UUID.randomUUID(),
            "alice",
            "Alice",
            "https://cdn.example.com/alice.png",
            UserStatus.ACTIVE,
            0L,
            null,
            null);

    UserJpaEntity saved = userJpaRepository.saveAndFlush(entity);

    assertThat(saved.id()).isNotNull();
    assertThat(saved.username()).isEqualTo("alice");
    assertThat(saved.version()).isEqualTo(0L);
    assertThat(saved.createdAt()).isNotNull();
    assertThat(saved.updatedAt()).isNotNull();
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=IdentityUserPersistenceIntegrationTest test`
Expected: FAIL because `UserStatus`, `UserJpaEntity`, and `UserJpaRepository` do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/me/bchieu/messaging/modules/identity/domain/UserStatus.java`:

```java
package me.bchieu.messaging.modules.identity.domain;

public enum UserStatus {
  ACTIVE,
  INACTIVE,
  BLOCKED
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaEntity.java`:

```java
package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "app_user")
public record UserJpaEntity(
    @Id UUID id,
    @Column(nullable = false, unique = true, length = 100) String username,
    @Column(name = "display_name", nullable = false) String displayName,
    @Column(name = "avatar_url", length = 512) String avatarUrl,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) UserStatus status,
    @Version @Column(nullable = false) Long version,
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt,
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) Instant updatedAt) {}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaRepository.java`:

```java
package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
  boolean existsByUsername(String username);

  Optional<UserJpaEntity> findByUsername(String username);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -Dtest=IdentityUserPersistenceIntegrationTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/modules/identity/domain/UserStatus.java src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaEntity.java src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/UserJpaRepository.java src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java
git commit -m "feat: add identity persistence baseline"
```

### Task 3: Add the domain repository and create/query application service

**Files:**
- Create: `src/main/java/me/bchieu/messaging/modules/identity/domain/User.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/domain/UserRepository.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserAlreadyExistsException.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserNotFoundException.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/application/CreateUserCommand.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/JpaUserRepository.java`
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java` with the create and get-by-username tests first:

```java
package me.bchieu.messaging.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IdentityUserApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldCreateUser() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "alice",
                      "displayName": "Alice",
                      "avatarUrl": "https://cdn.example.com/alice.png",
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("alice"))
        .andExpect(jsonPath("$.data.displayName").value("Alice"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void shouldGetUserByUsername() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "bob",
                      "displayName": "Bob",
                      "avatarUrl": null,
                      "status": "INACTIVE"
                    }
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/identity/users/by-username/{username}", "bob"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("bob"))
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: FAIL with 404 because the identity API and application service do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/me/bchieu/messaging/modules/identity/domain/User.java`:

```java
package me.bchieu.messaging.modules.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record User(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    UserStatus status,
    Long version,
    Instant createdAt,
    Instant updatedAt) {}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/domain/UserRepository.java`:

```java
package me.bchieu.messaging.modules.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  User save(User user);

  boolean existsByUsername(String username);

  Optional<User> findById(UUID id);

  Optional<User> findByUsername(String username);
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserAlreadyExistsException.java`:

```java
package me.bchieu.messaging.modules.identity.domain;

public class IdentityUserAlreadyExistsException extends RuntimeException {
  public IdentityUserAlreadyExistsException(String username) {
    super("Identity user already exists for username: " + username);
  }
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserNotFoundException.java`:

```java
package me.bchieu.messaging.modules.identity.domain;

import java.util.UUID;

public class IdentityUserNotFoundException extends RuntimeException {
  public IdentityUserNotFoundException(UUID userId) {
    super("Identity user not found for id: " + userId);
  }

  public IdentityUserNotFoundException(String username) {
    super("Identity user not found for username: " + username);
  }
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/application/CreateUserCommand.java`:

```java
package me.bchieu.messaging.modules.identity.application;

import me.bchieu.messaging.modules.identity.domain.UserStatus;

public record CreateUserCommand(
    String username, String displayName, String avatarUrl, UserStatus status) {}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java`:

```java
package me.bchieu.messaging.modules.identity.application;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.IdentityUserAlreadyExistsException;
import me.bchieu.messaging.modules.identity.domain.IdentityUserNotFoundException;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityUserService {
  private final UserRepository userRepository;

  public IdentityUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User create(CreateUserCommand command) {
    if (userRepository.existsByUsername(command.username())) {
      throw new IdentityUserAlreadyExistsException(command.username());
    }

    return userRepository.save(
        new User(
            UUID.randomUUID(),
            command.username(),
            command.displayName(),
            command.avatarUrl(),
            command.status(),
            0L,
            null,
            null));
  }

  @Transactional(readOnly = true)
  public User getById(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new IdentityUserNotFoundException(userId));
  }

  @Transactional(readOnly = true)
  public User getByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IdentityUserNotFoundException(username));
  }
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/JpaUserRepository.java`:

```java
package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepository implements UserRepository {
  private final UserJpaRepository userJpaRepository;

  public JpaUserRepository(UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public User save(User user) {
    UserJpaEntity saved = userJpaRepository.save(toEntity(user));
    return toDomain(saved);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userJpaRepository.existsByUsername(username);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return userJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userJpaRepository.findByUsername(username).map(this::toDomain);
  }

  private UserJpaEntity toEntity(User user) {
    return new UserJpaEntity(
        user.id(),
        user.username(),
        user.displayName(),
        user.avatarUrl(),
        user.status(),
        user.version(),
        user.createdAt(),
        user.updatedAt());
  }

  private User toDomain(UserJpaEntity entity) {
    return new User(
        entity.id(),
        entity.username(),
        entity.displayName(),
        entity.avatarUrl(),
        entity.status(),
        entity.version(),
        entity.createdAt(),
        entity.updatedAt());
  }
}
```

- [ ] **Step 4: Run test to verify it still fails for missing API**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: FAIL with 404 because the controller layer still does not exist.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/modules/identity/domain/User.java src/main/java/me/bchieu/messaging/modules/identity/domain/UserRepository.java src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserAlreadyExistsException.java src/main/java/me/bchieu/messaging/modules/identity/domain/IdentityUserNotFoundException.java src/main/java/me/bchieu/messaging/modules/identity/application/CreateUserCommand.java src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java src/main/java/me/bchieu/messaging/modules/identity/infrastructure/persistence/JpaUserRepository.java src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java
git commit -m "feat: add identity create and query service"
```

### Task 4: Add the API controller and request/response DTOs

**Files:**
- Create: `src/main/java/me/bchieu/messaging/modules/identity/api/CreateUserRequest.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/api/UserResponse.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java`
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java`

- [ ] **Step 1: Write the next failing test**

Extend `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java` with validation and not-found coverage:

```java
  @Test
  void shouldRejectBlankUsername() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "",
                      "displayName": "Alice",
                      "avatarUrl": null,
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void shouldReturnNotFoundForUnknownUsername() throws Exception {
    mockMvc
        .perform(get("/api/v1/identity/users/by-username/{username}", "missing-user"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: FAIL because the controller and DTOs do not exist.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/me/bchieu/messaging/modules/identity/api/CreateUserRequest.java`:

```java
package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank String displayName,
    String avatarUrl,
    @NotNull UserStatus status) {}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/api/UserResponse.java`:

```java
package me.bchieu.messaging.modules.identity.api;

import java.time.Instant;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

public record UserResponse(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.id(),
        user.username(),
        user.displayName(),
        user.avatarUrl(),
        user.status(),
        user.createdAt(),
        user.updatedAt());
  }
}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java`:

```java
package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.Valid;
import me.bchieu.messaging.common.response.ApiResponse;
import me.bchieu.messaging.modules.identity.application.CreateUserCommand;
import me.bchieu.messaging.modules.identity.application.IdentityUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/users")
public class IdentityUserController {
  private final IdentityUserService identityUserService;

  public IdentityUserController(IdentityUserService identityUserService) {
    this.identityUserService = identityUserService;
  }

  @PostMapping
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.success(
        UserResponse.from(
            identityUserService.create(
                new CreateUserCommand(
                    request.username(),
                    request.displayName(),
                    request.avatarUrl(),
                    request.status()))));
  }

  @GetMapping("/{userId}")
  public ApiResponse<UserResponse> getById(@PathVariable java.util.UUID userId) {
    return ApiResponse.success(UserResponse.from(identityUserService.getById(userId)));
  }

  @GetMapping("/by-username/{username}")
  public ApiResponse<UserResponse> getByUsername(@PathVariable String username) {
    return ApiResponse.success(UserResponse.from(identityUserService.getByUsername(username)));
  }
}
```

Modify `src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java`:

```java
package me.bchieu.messaging.common.exception;

import java.util.stream.Collectors;
import me.bchieu.messaging.common.response.ApiResponse;
import me.bchieu.messaging.modules.identity.domain.IdentityUserAlreadyExistsException;
import me.bchieu.messaging.modules.identity.domain.IdentityUserNotFoundException;
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
    // Gom toàn bộ lỗi validate để dễ theo dõi từ phía client.
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, message, null));
  }

  @ExceptionHandler(IdentityUserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserAlreadyExists(
      IdentityUserAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }

  @ExceptionHandler(IdentityUserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserNotFound(
      IdentityUserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: PASS for create, get-by-username, validation, and unknown-username scenarios.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/modules/identity/api/CreateUserRequest.java src/main/java/me/bchieu/messaging/modules/identity/api/UserResponse.java src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java
git commit -m "feat: add identity create and query api"
```

### Task 5: Add the update flow and get-by-id coverage

**Files:**
- Create: `src/main/java/me/bchieu/messaging/modules/identity/application/UpdateUserCommand.java`
- Create: `src/main/java/me/bchieu/messaging/modules/identity/api/UpdateUserRequest.java`
- Modify: `src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java`
- Modify: `src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java`
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

Extend `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java` with update and get-by-id tests:

```java
  @Test
  void shouldUpdateMutableFieldsOnly() throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/identity/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "username": "carol",
                          "displayName": "Carol",
                          "avatarUrl": null,
                          "status": "ACTIVE"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String userId = com.jayway.jsonpath.JsonPath.read(response, "$.data.id");

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/identity/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "displayName": "Carol Updated",
                      "avatarUrl": "https://cdn.example.com/carol.png",
                      "status": "BLOCKED"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("carol"))
        .andExpect(jsonPath("$.data.displayName").value("Carol Updated"))
        .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example.com/carol.png"))
        .andExpect(jsonPath("$.data.status").value("BLOCKED"));
  }

  @Test
  void shouldGetUserById() throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/identity/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "username": "dave",
                          "displayName": "Dave",
                          "avatarUrl": null,
                          "status": "ACTIVE"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String userId = com.jayway.jsonpath.JsonPath.read(response, "$.data.id");

    mockMvc
        .perform(get("/api/v1/identity/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("dave"));
  }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: FAIL because update request/command and PUT endpoint do not exist.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/java/me/bchieu/messaging/modules/identity/application/UpdateUserCommand.java`:

```java
package me.bchieu.messaging.modules.identity.application;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

public record UpdateUserCommand(
    UUID userId, String displayName, String avatarUrl, UserStatus status) {}
```

Create `src/main/java/me/bchieu/messaging/modules/identity/api/UpdateUserRequest.java`:

```java
package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

public record UpdateUserRequest(@NotBlank String displayName, String avatarUrl, @NotNull UserStatus status) {}
```

Modify `src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java`:

```java
package me.bchieu.messaging.modules.identity.application;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.IdentityUserAlreadyExistsException;
import me.bchieu.messaging.modules.identity.domain.IdentityUserNotFoundException;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityUserService {
  private final UserRepository userRepository;

  public IdentityUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User create(CreateUserCommand command) {
    if (userRepository.existsByUsername(command.username())) {
      throw new IdentityUserAlreadyExistsException(command.username());
    }

    return userRepository.save(
        new User(
            UUID.randomUUID(),
            command.username(),
            command.displayName(),
            command.avatarUrl(),
            command.status(),
            0L,
            null,
            null));
  }

  @Transactional
  public User update(UpdateUserCommand command) {
    User existing =
        userRepository.findById(command.userId()).orElseThrow(() -> new IdentityUserNotFoundException(command.userId()));

    return userRepository.save(
        new User(
            existing.id(),
            existing.username(),
            command.displayName(),
            command.avatarUrl(),
            command.status(),
            existing.version(),
            existing.createdAt(),
            existing.updatedAt()));
  }

  @Transactional(readOnly = true)
  public User getById(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new IdentityUserNotFoundException(userId));
  }

  @Transactional(readOnly = true)
  public User getByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IdentityUserNotFoundException(username));
  }
}
```

Modify `src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java`:

```java
package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.Valid;
import java.util.UUID;
import me.bchieu.messaging.common.response.ApiResponse;
import me.bchieu.messaging.modules.identity.application.CreateUserCommand;
import me.bchieu.messaging.modules.identity.application.IdentityUserService;
import me.bchieu.messaging.modules.identity.application.UpdateUserCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/users")
public class IdentityUserController {
  private final IdentityUserService identityUserService;

  public IdentityUserController(IdentityUserService identityUserService) {
    this.identityUserService = identityUserService;
  }

  @PostMapping
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.success(
        UserResponse.from(
            identityUserService.create(
                new CreateUserCommand(
                    request.username(),
                    request.displayName(),
                    request.avatarUrl(),
                    request.status()))));
  }

  @PutMapping("/{userId}")
  public ApiResponse<UserResponse> update(
      @PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
    return ApiResponse.success(
        UserResponse.from(
            identityUserService.update(
                new UpdateUserCommand(
                    userId, request.displayName(), request.avatarUrl(), request.status()))));
  }

  @GetMapping("/{userId}")
  public ApiResponse<UserResponse> getById(@PathVariable UUID userId) {
    return ApiResponse.success(UserResponse.from(identityUserService.getById(userId)));
  }

  @GetMapping("/by-username/{username}")
  public ApiResponse<UserResponse> getByUsername(@PathVariable String username) {
    return ApiResponse.success(UserResponse.from(identityUserService.getByUsername(username)));
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: PASS for update and get-by-id scenarios.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/bchieu/messaging/modules/identity/application/UpdateUserCommand.java src/main/java/me/bchieu/messaging/modules/identity/api/UpdateUserRequest.java src/main/java/me/bchieu/messaging/modules/identity/application/IdentityUserService.java src/main/java/me/bchieu/messaging/modules/identity/api/IdentityUserController.java src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java
git commit -m "feat: add identity update flow"
```

### Task 6: Cover duplicate username and missing user API behavior

**Files:**
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java`
- Modify: `src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: Write the failing test**

Extend `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java` with duplicate and missing-id tests:

```java
  @Test
  void shouldRejectDuplicateUsername() throws Exception {
    String payload =
        """
        {
          "username": "erin",
          "displayName": "Erin",
          "avatarUrl": null,
          "status": "ACTIVE"
        }
        """;

    mockMvc
        .perform(post("/api/v1/identity/users").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/api/v1/identity/users").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void shouldReturnNotFoundForUnknownId() throws Exception {
    mockMvc
        .perform(get("/api/v1/identity/users/{userId}", java.util.UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: FAIL if duplicate conflict or missing-id mapping is not fully wired.

- [ ] **Step 3: Write minimal implementation**

If the exception handler from Task 4 already covers both cases, keep the production code unchanged and only normalize messages if needed. The expected `GlobalExceptionHandler` shape is:

```java
  @ExceptionHandler(IdentityUserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserAlreadyExists(
      IdentityUserAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }

  @ExceptionHandler(IdentityUserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserNotFound(
      IdentityUserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -Dtest=IdentityUserApiIntegrationTest test`
Expected: PASS for duplicate username and missing-id scenarios.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java
git commit -m "test: cover identity error responses"
```

### Task 7: Run the focused verification suite and repo quality checks

**Files:**
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java`
- Test: `src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`

- [ ] **Step 1: Run the focused identity tests**

Run: `./mvnw -Dtest=MigrationStructureIntegrationTest,IdentityUserPersistenceIntegrationTest,IdentityUserApiIntegrationTest test`
Expected: PASS.

- [ ] **Step 2: Run the full verification pipeline**

Run: `./mvnw verify`
Expected: PASS with tests, Spotless, Checkstyle, and SpotBugs all green.

- [ ] **Step 3: Review changed files before committing the verification checkpoint**

Run: `git diff -- src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java src/main/java/me/bchieu/messaging/modules/identity src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java`
Expected: diff only shows the identity slice and related tests/exception wiring.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V20260418_0002__create_app_user_table.sql src/main/java/me/bchieu/messaging/common/exception/GlobalExceptionHandler.java src/main/java/me/bchieu/messaging/modules/identity src/test/java/me/bchieu/messaging/integration/IdentityUserApiIntegrationTest.java src/test/java/me/bchieu/messaging/integration/IdentityUserPersistenceIntegrationTest.java src/test/java/me/bchieu/messaging/integration/MigrationStructureIntegrationTest.java
git commit -m "feat: implement phase 1 identity module slice"
```

## Self-Review

- **Spec coverage:** migration, UUID primary key, immutable unique username, `ACTIVE/INACTIVE/BLOCKED`, internal `version`, create/update/get-by-id/get-by-username, exception mapping, and integration tests are all covered by Tasks 1-7.
- **Placeholder scan:** no `TODO`, `TBD`, or implicit “add tests later” steps remain; every code-writing step includes concrete file paths and code.
- **Type consistency:** `UserStatus`, `User`, `CreateUserCommand`, `UpdateUserCommand`, `IdentityUserService`, `UserResponse`, and REST paths are named consistently across all tasks.
