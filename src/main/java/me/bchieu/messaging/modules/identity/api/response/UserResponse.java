package me.bchieu.messaging.modules.identity.api.response;

import java.time.Instant;
import java.util.UUID;
import me.bchieu.messaging.modules.identity.domain.User;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

/** Response payload for identity user APIs. */
public record UserResponse(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    UserStatus status,
    Long version,
    Instant createdAt,
    Instant updatedAt) {

  /** Maps a domain user to an API response payload. */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.id(),
        user.username(),
        user.displayName(),
        user.avatarUrl(),
        user.status(),
        user.version(),
        user.createdAt(),
        user.updatedAt());
  }
}
