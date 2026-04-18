package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.UserStatus;
import me.bchieu.messaging.modules.identity.infrastructure.persistence.UserJpaEntity;
import me.bchieu.messaging.modules.identity.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"})
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
            null,
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
