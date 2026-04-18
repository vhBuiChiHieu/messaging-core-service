package me.bchieu.messaging.modules.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
/** JPA-backed repository adapter for identity users. */
public class JpaUserRepository implements UserRepository {
  private final UserJpaRepository userJpaRepository;

  /** Creates the adapter with the Spring Data repository dependency. */
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
