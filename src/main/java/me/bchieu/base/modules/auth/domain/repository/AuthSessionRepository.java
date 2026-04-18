package me.bchieu.base.modules.auth.domain.repository;

import java.util.Optional;
import me.bchieu.base.modules.auth.domain.model.AuthSession;

public interface AuthSessionRepository {

  void save(AuthSession authSession);

  Optional<AuthSession> findByToken(String token);
}
