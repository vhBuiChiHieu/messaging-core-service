package me.bchieu.messaging.modules.identity.domain;

import java.time.Instant;
import java.util.UUID;

/** Domain model for an identity user. */
public record User(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    UserStatus status,
    Long version,
    Instant createdAt,
    Instant updatedAt) {}
