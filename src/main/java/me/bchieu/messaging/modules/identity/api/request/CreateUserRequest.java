package me.bchieu.messaging.modules.identity.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.bchieu.messaging.modules.identity.domain.UserStatus;

/** Request payload for creating an identity user. */
public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank String displayName,
    String avatarUrl,
    @NotNull UserStatus status) {}
