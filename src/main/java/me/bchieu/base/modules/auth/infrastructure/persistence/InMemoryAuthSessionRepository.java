package me.bchieu.base.modules.auth.infrastructure.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import me.bchieu.base.modules.auth.domain.model.AuthSession;
import me.bchieu.base.modules.auth.domain.repository.AuthSessionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAuthSessionRepository implements AuthSessionRepository {

  // Dùng ConcurrentHashMap để an toàn khi có nhiều request login đồng thời.
  private final Map<String, AuthSession> sessionsByToken = new ConcurrentHashMap<>();

  @Override
  public void save(AuthSession authSession) {
    sessionsByToken.put(authSession.token(), authSession);
  }

  @Override
  public Optional<AuthSession> findByToken(String token) {
    return Optional.ofNullable(sessionsByToken.get(token));
  }
}
