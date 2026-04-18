package me.bchieu.messaging.modules.identity.application;

import me.bchieu.messaging.modules.identity.domain.UserStatus;

/** Command data for updating an identity user. */
public record UpdateUserCommand(
    String username, String displayName, String avatarUrl, UserStatus status) {}
