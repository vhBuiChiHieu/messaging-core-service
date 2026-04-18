package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

/** Request payload for updating an identity user. */
public record UpdateUserRequest(
    @NotBlank String username,
    @NotBlank String displayName,
    String avatarUrl,
    @NotNull UserStatus status) {}
