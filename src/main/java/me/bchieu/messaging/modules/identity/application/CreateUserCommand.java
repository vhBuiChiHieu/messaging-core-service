package me.bchieu.messaging.modules.identity.application;

import me.bchieu.messaging.modules.identity.domain.UserStatus;

/** Command data for creating an identity user. */
public record CreateUserCommand(
    String username, String displayName, String avatarUrl, UserStatus status) {}
