package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for identity user entities. */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
  /** Checks whether an entity already exists for the username. */
  boolean existsByUsername(String username);

  /** Finds an entity by username. */
  Optional<UserJpaEntity> findByUsername(String username);
}
