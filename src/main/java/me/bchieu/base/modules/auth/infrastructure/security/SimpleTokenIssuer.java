package me.bchieu.base.modules.auth.infrastructure.security;

import java.util.UUID;
import me.bchieu.base.modules.auth.domain.service.TokenIssuer;
import org.springframework.stereotype.Component;

@Component
public class SimpleTokenIssuer implements TokenIssuer {

  @Override
  public String issue(String username) {
    return username + "-" + UUID.randomUUID();
  }
}
