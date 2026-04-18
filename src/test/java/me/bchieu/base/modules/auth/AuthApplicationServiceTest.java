package me.bchieu.base.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;

import me.bchieu.base.modules.auth.application.dto.LoginCommand;
import me.bchieu.base.modules.auth.application.service.AuthApplicationService;
import me.bchieu.base.modules.auth.domain.model.AuthSession;
import me.bchieu.base.modules.auth.domain.repository.AuthSessionRepository;
import me.bchieu.base.modules.auth.domain.service.TokenIssuer;
import me.bchieu.base.modules.auth.infrastructure.persistence.InMemoryAuthSessionRepository;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceTest {

  @Test
  void shouldIssueTokenAndStoreSessionWhenLogin() {
    AuthSessionRepository repository = new InMemoryAuthSessionRepository();
    TokenIssuer tokenIssuer = username -> "token-for-" + username;
    AuthApplicationService service = new AuthApplicationService(repository, tokenIssuer);

    LoginCommand command = new LoginCommand("admin", "secret");

    String token = service.login(command);
    AuthSession storedSession = repository.findByToken(token).orElseThrow();

    assertThat(token).isEqualTo("token-for-admin");
    assertThat(storedSession.username()).isEqualTo("admin");
    assertThat(storedSession.token()).isEqualTo("token-for-admin");
  }
}
