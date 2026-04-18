package me.bchieu.base.modules.auth.application.service;

import me.bchieu.base.modules.auth.application.dto.LoginCommand;
import me.bchieu.base.modules.auth.domain.model.AuthSession;
import me.bchieu.base.modules.auth.domain.repository.AuthSessionRepository;
import me.bchieu.base.modules.auth.domain.service.TokenIssuer;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

  private final AuthSessionRepository authSessionRepository;
  private final TokenIssuer tokenIssuer;

  public AuthApplicationService(
      AuthSessionRepository authSessionRepository, TokenIssuer tokenIssuer) {
    this.authSessionRepository = authSessionRepository;
    this.tokenIssuer = tokenIssuer;
  }

  public String login(LoginCommand command) {
    String token = tokenIssuer.issue(command.username());
    authSessionRepository.save(new AuthSession(command.username(), token));
    return token;
  }
}
