package me.bchieu.messaging.modules.identity.domain;

import java.util.UUID;

/** Raised when an identity user cannot be found. */
public class IdentityUserNotFoundException extends RuntimeException {
  /** Creates the exception for a missing user id. */
  public IdentityUserNotFoundException(UUID userId) {
    super("Identity user not found for id: " + userId);
  }

  /** Creates the exception for a missing username. */
  public IdentityUserNotFoundException(String username) {
    super("Identity user not found for username: " + username);
  }
}
