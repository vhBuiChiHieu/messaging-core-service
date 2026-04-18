package me.bchieu.messaging.modules.identity.domain;

import java.util.Optional;
import java.util.UUID;

/** Repository contract for identity users. */
public interface UserRepository {
  /** Persists an identity user. */
  User save(User user);

  /** Checks whether a username already exists. */
  boolean existsByUsername(String username);

  /** Finds an identity user by id. */
  Optional<User> findById(UUID id);

  /** Finds an identity user by username. */
  Optional<User> findByUsername(String username);
}
