package me.bchieu.messaging.modules.identity.domain;

/** Raised when an identity username is already in use. */
public class IdentityUserAlreadyExistsException extends RuntimeException {
  /** Creates the exception for a duplicated username. */
  public IdentityUserAlreadyExistsException(String username) {
    super("Identity user already exists for username: " + username);
  }
}
