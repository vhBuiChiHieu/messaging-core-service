package me.bchieu.base.modules.auth.application.mapper;

import me.bchieu.base.modules.auth.application.dto.LoginCommand;

public final class AuthMapper {

  private AuthMapper() {}

  public static LoginCommand toLoginCommand(String username, String password) {
    return new LoginCommand(username, password);
  }
}
