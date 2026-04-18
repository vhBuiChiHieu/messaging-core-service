package me.bchieu.messaging.modules.identity.application;

import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.IdentityUserAlreadyExistsException;
import me.bchieu.messaging.modules.identity.domain.IdentityUserNotFoundException;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for identity user operations. */
@Service
public class IdentityUserService {
  private final UserRepository userRepository;

  /** Creates the service with the identity user repository dependency. */
  public IdentityUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** Creates a new identity user. */
  @Transactional
  public User create(CreateUserCommand command) {
    if (userRepository.existsByUsername(command.username())) {
      throw new IdentityUserAlreadyExistsException(command.username());
    }

    try {
      return userRepository.save(
          new User(
              UUID.randomUUID(),
              command.username(),
              command.displayName(),
              command.avatarUrl(),
              command.status(),
              null,
              null,
              null));
    } catch (DataIntegrityViolationException exception) {
      throw new IdentityUserAlreadyExistsException(command.username());
    }
  }

  /** Returns an identity user by id. */
  @Transactional(readOnly = true)
  public User getById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IdentityUserNotFoundException(userId));
  }

  /** Returns an identity user by username. */
  @Transactional(readOnly = true)
  public User getByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IdentityUserNotFoundException(username));
  }
}
